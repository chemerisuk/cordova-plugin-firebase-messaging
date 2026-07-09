#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    const opts = context.opts || {};
    const projectRoot = context.opts.projectRoot;
    const iosPlatformPath = path.join(projectRoot, 'platforms', 'ios');

    console.log('package.json', require(path.join(projectRoot, 'package.json')));

    // 2. Reuse standard Node.js argv parser logic (simulate clean env parsing)
    const varName = 'IOS_FIREBASE_POD_VERSION';
    let targetValue = null;

    if (Array.isArray(process.argv)) {
        // Parse raw terminal arguments into a key-value dictionary object
        const cliVars = process.argv.reduce((acc, arg, index, arr) => {
            if (arg === '--variable' && arr[index + 1]) {
                const [k, v] = arr[index + 1].split('=');
                if (k) acc[k.trim()] = v;
            } else if (arg.startsWith('--variable=')) {
                const [k, v] = arg.replace('--variable=', '').split('=');
                if (k) acc[k.trim()] = v;
            }
            return acc;
        }, {});

        // Safely extract and clean the target variable if it exists
        if (cliVars[varName]) {
            targetValue = cliVars[varName].replace(/^['"]|['"]$/g, '').trim();
        }
    }

    // Fallback to plugin.xml preference default value if CLI variable is missing
    if (!targetValue && opts.plugin?.pluginInfo) {
        const pluginInfo = opts.plugin.pluginInfo;
        targetValue = pluginInfo.getPreferences('ios')[varName] || pluginInfo.getPreferences()[varName];
    }

    const pluginId = opts.plugin && opts.plugin.id;
    if (!targetValue || !pluginId) return;

    // 3. Define paths where Package.swift can reside
    const packagePaths = [
        path.join(opts.plugin.dir, 'Package.swift'),
        path.join(iosPlatformPath, 'packages', pluginId, 'Package.swift')
    ];

    // 4. Perform direct replace for $IOS_FIREBASE_POD_VERSION inline
    const searchRegex = new RegExp('\\$' + varName, 'g');

    packagePaths.forEach(packagePath => {
        if (!fs.existsSync(packagePath)) return;

        let content = fs.readFileSync(packagePath, 'utf8');

        if (searchRegex.test(content)) {
            content = content.replace(searchRegex, targetValue);
            fs.writeFileSync(packagePath, content, 'utf8');
        }
    });
};
