#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    const opts = context.opts || {};
    const projectRoot = opts.projectRoot || path.resolve(__dirname, '../../');

    const pkgJSON = require(path.join(projectRoot, 'package.json'));

    console.log('pkgJSON', pkgJSON);

    // 1. Quick exit if not an iOS environment
    const isIosTarget = opts.platforms && opts.platforms.includes('ios');
    const iosPlatformPath = path.join(projectRoot, 'platforms', 'ios');
    if (!isIosTarget && !fs.existsSync(iosPlatformPath)) return;

    // 2. Safely get preferences from pluginInfo
    const pluginInfo = opts.plugin && opts.plugin.pluginInfo;
    if (!pluginInfo) return;

    const prefs = { ...(pluginInfo.getPreferences() || {}), ...(pluginInfo.getPreferences('ios') || {}) };
    const varNames = Object.keys(prefs);
    if (varNames.length === 0) return;

    // 3. Define dynamic paths based on dynamic plugin ID
    const pluginId = opts.plugin.id;
    if (!pluginId) return;

    const packagePaths = [
        path.join(opts.plugin.dir, 'Package.swift'),
        path.join(iosPlatformPath, 'packages', pluginId, 'Package.swift')
    ];

    // 4. Update Package.swift inline in all locations
    packagePaths.forEach(packagePath => {
        if (!fs.existsSync(packagePath)) return;

        let content = fs.readFileSync(packagePath, 'utf8');
        let isModified = false;

        varNames.forEach(varName => {
            const targetValue = process.env[varName] || opts.cli_variables?.[varName] || prefs[varName];
            if (!targetValue) return;

            // 1. RegExp for the first execution (matches the placeholder with the trailing comment)
            const placeholderRegex = new RegExp('\\$' + varName + '(?=.*\\/\\/\\s*cpm:' + varName + ')', 'g');

            // 2. RegExp for subsequent executions (matches exact: "version" targeted by the specific trailing comment)
            const exactCommentRegex = new RegExp('(exact:\\s*["\'])([^"\']+)(["\'](?=.*\\/\\/\\s*cpm:' + varName + '))', 'g');

            if (placeholderRegex.test(content)) {
                content = content.replace(placeholderRegex, targetValue);
                isModified = true;
            } else if (exactCommentRegex.test(content)) {
                content = content.replace(exactCommentRegex, `$1${targetValue}$3`);
                isModified = true;
            }
        });

        if (isModified) {
            fs.writeFileSync(packagePath, content, 'utf8');
        }
    });
};
