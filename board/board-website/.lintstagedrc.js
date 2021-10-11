/**
 * lintstage config
 * @description eslint codes before commit
 */

const lintStaged = require('lint-staged');

async function startLinting() {
    try {
        const success = await lintStaged({
            allowEmpty: false,
            concurrent: true,
            config: {
                '*.{js,vue}': [
                    // 'prettier --write',
                    'eslint --fix --cache --fix-type suggestion,layout,problem .',
                ],
            },
            debug: false,
            cwd: process.cwd(),
            maxArgLength: null,
            relative: false,
            shell: false,
            quiet: true,
            stash: true,
            verbose: false,
        });

        if (success) {
            console.log('Linting was successful!\n');
            process.exit(0);
        } else {
            console.log('Linting failed!\n');
            process.exit(1);
        }
    } catch (e) {
        // Failed to load configuration
        console.error(e);
        process.exit(1);
    }
}

startLinting();
