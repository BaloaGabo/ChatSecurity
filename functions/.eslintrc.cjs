// functions/.eslintrc.js
module.exports = {
    env: { node: true, es6: true },
    parserOptions: { ecmaVersion: 2020 },
    extends: ["eslint:recommended", "google"],
    rules: {
        "max-len": ["error", { code: 120 }],
        "indent": ["error", 2],
        "comma-dangle": ["error", "always-multiline"]
    }
};

