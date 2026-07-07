#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    const opts = context.opts || {};
    const projectRoot = opts.projectRoot || path.resolve(__dirname, '../../');

    // 1. Quick exit if not an iOS environment
    const isIosTarget = opts.platforms && opts.platforms.includes('ios');
    const hasIosDir = fs.existsSync(path.join(projectRoot, 'platforms', 'ios'));
    if (!isIosTarget && !hasIosDir) return;

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

    // 3. Update Package.swift inline
    const packagePath = path.join(opts.plugin.dir, 'Package.swift');
    if (fs.existsSync(packagePath)) {
        let content = fs.readFileSync(packagePath, 'utf8');
        const searchRegex = /\$IOS_FIREBASE_POD_VERSION/g;

        if (searchRegex.test(content)) {
            content = content.replace(searchRegex, targetValue);
            fs.writeFileSync(packagePath, content, 'utf8');
            console.log(`✅ Package.swift updated with version: "${targetValue}"`);
        } else {
            console.warn(`⚠️ Placeholder $IOS_FIREBASE_POD_VERSION not found.`);
        }
    } else {
        console.warn('❌ Package.swift not found.');
    }
};
