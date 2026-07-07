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
    const searchRegex = /\$IOS_FIREBASE_POD_VERSION/g;

    const pluginId = opts.plugin.id;
    if (!pluginId) return;

    const packagePaths = [
        path.join(opts.plugin.dir, 'Package.swift'),
        path.join(projectRoot, 'platforms', 'ios', 'packages', pluginId, 'Package.swift')
    ];

    packagePaths.forEach(packagePath => {
        if (fs.existsSync(packagePath)) {
            let content = fs.readFileSync(packagePath, 'utf8');

            if (searchRegex.test(content)) {
                content = content.replace(searchRegex, targetValue);
                fs.writeFileSync(packagePath, content, 'utf8');
            }
        }
    });
};
