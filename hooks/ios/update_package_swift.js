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

    // 2. Fetch preferences defined in plugin.xml (both iOS-specific and global)
    const pluginInfo = opts.plugin && opts.plugin.pluginInfo;
    if (!pluginInfo) return;

    const prefs = {
        ...(pluginInfo.getPreferences() || {}),
        ...(pluginInfo.getPreferences('ios') || {})
    };

    const varNames = Object.keys(prefs);
    if (varNames.length === 0) return;

    // 3. Define all possible paths where Package.swift can reside
    const pluginId = opts.plugin.id;
    if (!pluginId) return;

    const packagePaths = [
        path.join(opts.plugin.dir, 'Package.swift'),
        path.join(iosPlatformPath, 'packages', pluginId, 'Package.swift')
    ];

    const cliVariables = opts.cli_variables || {};

    // 4. Loop through all paths and apply replacements for plugin preferences
    packagePaths.forEach(packagePath => {
        if (!fs.existsSync(packagePath)) return;

        let content = fs.readFileSync(packagePath, 'utf8');
        let isModified = false;

        varNames.forEach(varName => {
            // CLI value takes precedence, falls back to plugin.xml preference value
            const targetValue = cliVariables[varName] || prefs[varName];
            if (!targetValue) return;

            const searchRegex = new RegExp('\\$' + varName, 'g');

            if (searchRegex.test(content)) {
                content = content.replace(searchRegex, targetValue);
                isModified = true;
            }
        });

        if (isModified) {
            fs.writeFileSync(packagePath, content, 'utf8');
        }
    });
};
