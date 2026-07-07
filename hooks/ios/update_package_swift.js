#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    const opts = context.opts || {};

    if (!opts.platforms || !opts.platforms.includes('ios')) return;

    const varName = 'IOS_FIREBASE_POD_VERSION';
    const cliVariables = opts.cli_variables || {};
    let targetValue = cliVariables[varName];

    if (!targetValue) {
        const { PluginInfoProvider } = require('cordova-common');
        const pluginInfo = new PluginInfoProvider().get(opts.plugin.dir);
        targetValue = pluginInfo.getPreferences('ios')[varName] || pluginInfo.getPreferences()[varName];
    }

    if (!targetValue) return;

    const projectRoot = opts.projectRoot || path.resolve(__dirname, '../../');
    const packagePath = path.join(projectRoot, 'platforms', 'ios', 'packages', 'cordova-plugin-firebase-messaging', 'Package.swift');

    if (fs.existsSync(packagePath)) {
        let content = fs.readFileSync(packagePath, 'utf8');
        const searchRegex = /\$IOS_FIREBASE_POD_VERSION/g;

        if (searchRegex.test(content)) {
            content = content.replace(searchRegex, targetValue);
            fs.writeFileSync(packagePath, content, 'utf8');
        }
    }
};
