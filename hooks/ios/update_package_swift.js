#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    const opts = context.opts || {};
    const projectRoot = opts.projectRoot || path.resolve(__dirname, '../../');

    // 1. Quick exit if not an iOS environment
    const isIosTarget = opts.platforms && opts.platforms.includes('ios');
    const iosPlatformPath = path.join(projectRoot, 'platforms', 'ios');
    if (!isIosTarget && !fs.existsSync(iosPlatformPath)) return;

    // 2. Extract variable value from opts.cmdLine or fallback to pluginInfo preferences
    const varName = 'IOS_FIREBASE_POD_VERSION';
    let targetValue = null;

    if (opts.cmdLine) {
        // Matches both --variable KEY=VALUE and --variable=KEY=VALUE
        const varRegex = new RegExp('--variable\\s+' + varName + '=["\']?([^"\'\\s]+)["\']?|--variable=' + varName + '=["\']?([^"\'\\s]+)["\']?');
        const match = opts.cmdLine.match(varRegex);
        if (match) {
            targetValue = match[1] || match[2];
        }
    }

    // Fallback to plugin.xml preference default value if not found in cmdLine
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
