const eslint = require('@eslint/js');
const globals = require('globals');

module.exports = [
    {
        ignores: ['dist/'],
    },
    eslint.configs.recommended,
    {
        languageOptions: {
            ecmaVersion: 2018,
            globals: {
                ...globals.node,
                ...globals.jest,
            },
        },
    },
];
