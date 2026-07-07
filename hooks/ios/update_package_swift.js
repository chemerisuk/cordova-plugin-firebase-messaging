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

    // 2. Fetch the variable value (CLI choice overrides plugin.xml default)
    const varName = 'IOS_FIREBASE_POD_VERSION';
    const cliVariables = opts.cli_variables || {};
    let targetValue = cliVariables[varName];

    if (!targetValue) {
        const { PluginInfoProvider } = require('cordova-common');
        const pluginInfo = new PluginInfoProvider().get(opts.plugin.dir);
        targetValue = pluginInfo.getPreferences('ios')[varName] || pluginInfo.getPreferences()[varName];
    }

    if (!targetValue) {
        console.warn(`⚠️ Variable ${varName} not found. Skipping.`);
        return;
    }

    // 3. Define all possible paths where Package.swift can reside
    const searchRegex = /\$IOS_FIREBASE_POD_VERSION/g;
    const packagePaths = [
        path.join(opts.plugin.dir, 'Package.swift'),
        path.join(iosPlatformPath, 'packages', 'cordova-plugin-firebase-messaging', 'Package.swift')
    ];

    // 4. Update Package.swift inline in all locations
    packagePaths.forEach(packagePath => {
        if (fs.existsSync(packagePath)) {
            let content = fs.readFileSync(packagePath, 'utf8');

            if (searchRegex.test(content)) {
                content = content.replace(searchRegex, targetValue);
                fs.writeFileSync(packagePath, content, 'utf8');
                console.log(`✅ Package.swift updated at: ${packagePath}`);
            }
        }
    });
};
