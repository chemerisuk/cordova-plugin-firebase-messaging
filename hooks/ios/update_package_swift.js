#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    const opts = context.opts || {};
    const pluginId = opts.plugin && opts.plugin.id;
    const iosPlatformPath = path.join(opts.projectRoot, 'platforms', 'ios');

    const iosJsonPath = path.join(iosPlatformPath, 'ios.json');
    const iosJson = JSON.parse(fs.readFileSync(iosJsonPath, 'utf8'));
    const pluginVariables = iosJson.installed_plugins[pluginId] || {};

    const packagePaths = [
        path.join(opts.plugin.dir, 'Package.swift'),
        path.join(iosPlatformPath, 'packages', pluginId, 'Package.swift')
    ];

    packagePaths.forEach(packagePath => {
        if (!fs.existsSync(packagePath)) return;

        let content = fs.readFileSync(packagePath, 'utf8');

        for (const varName in pluginVariables) {
            content = content.replaceAll(`${varName}`, pluginVariables[varName]);
        }

        fs.writeFileSync(packagePath, content, 'utf8');
    });
};
