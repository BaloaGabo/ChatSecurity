const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// Envía un payload mixto (notification + data) a un token específico.
async function sendToToken(token, title, body, data = {}) {
    const message = {
        notification: { title, body },
        data,
        token
    };
    try {
        await admin.messaging().send(message);
        console.log("✅ Notificación enviada a", token);
    } catch (err) {
        console.error("❌ Error al enviar a", token, err);
    }
}

// 1) Mensajes privados: solo un disparo (uid1 < uid2)
exports.onPrivateMessage = functions
    .database.ref("/MensajesIndividuales/{uid1}/{uid2}/messages/{messageId}")
    .onCreate(async (snap, ctx) => {
        const msg = snap.val();
        const { uid1, uid2, messageId } = ctx.params;
        if (uid1 > uid2) return null;  // evita doble notificación

        const senderId = msg.senderId;
        const recipientUid = senderId === uid1 ? uid2 : uid1;

        // Obtén nombre remitente
        const senderName = (await admin
            .database()
            .ref(`/Usuarios/${senderId}/nombres`)
            .once("value"))
            .val() || "Alguien";

        // Token del destinatario
        const token = (await admin
            .database()
            .ref(`/Usuarios/${recipientUid}/fcmToken`)
            .once("value"))
            .val();
        if (!token) return null;

        // Prepara payload
        const preview = (msg.text || "").slice(0, 10) + ((msg.text || "").length > 10 ? "…" : "");
        const title = senderName;
        const body = preview;
        const data = {
            chatType: "private",
            chatId: `${uid1}_${uid2}`,
            messageId,
            senderId,
            senderName
        };

        return sendToToken(token, title, body, data);
    });

// 2) Mensajes grupales
exports.onGroupMessage = functions
    .database.ref("/ChatsGrupales/{groupId}/messages/{messageId}")
    .onCreate(async (snap, ctx) => {
        const msg = snap.val();
        const { groupId, messageId } = ctx.params;
        const senderId = msg.senderId;

        // Nombre del grupo y remitente
        const [groupName, senderName] = await Promise.all([
            admin.database().ref(`/ChatsGrupales/${groupId}/groupName`).once("value").then(s => s.val() || "Grupo"),
            admin.database().ref(`/Usuarios/${senderId}/nombres`).once("value").then(s => s.val() || "Miembro")
        ]);

        // Tokens de miembros (excepto remitente)
        const members = Object.keys((await admin
            .database().ref(`/ChatsGrupales/${groupId}/members`).once("value"))
            .val() || {});

        const title = groupName;
        const preview = (msg.text || "").slice(0, 10) + ((msg.text || "").length > 10 ? "…" : "");
        const body = `${senderName}: ${preview}`;
        const data = { chatType: "group", groupId, messageId, senderId, senderName, groupName };

        const promises = members
            .filter(uid => uid !== senderId)
            .map(uid =>
                admin.database().ref(`/Usuarios/${uid}/fcmToken`).once("value")
                    .then(s => s.val())
                    .then(token => token ? sendToToken(token, title, body, data) : null)
            );
        return Promise.all(promises);
    });

