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

    // 2. Safely get references from pluginInfo (for fallback defaults)
    const pluginInfo = opts.plugin && opts.plugin.pluginInfo;
    if (!pluginInfo) return;

    const prefs = { ...(pluginInfo.getPreferences() || {}), ...(pluginInfo.getPreferences('ios') || {}) };
    const varNames = Object.keys(prefs);
    if (varNames.length === 0) return;

    // 3. Define paths based on dynamic plugin ID
    const pluginId = opts.plugin.id;
    if (!pluginId) return;

    const packagePaths = [
        path.join(opts.plugin.dir, 'Package.swift'),
        path.join(iosPlatformPath, 'packages', pluginId, 'Package.swift')
    ];

    // 4. Parse variables directly from CORDOVA_CMDLINE to avoid Cordova lifecycle bugs
    const cmdLine = process.env.CORDOVA_CMDLINE || '';
    const cliVariables = {};

    console.log('process.env', process.env);
    console.log('process.argv', process.argv);

    varNames.forEach(varName => {
        // Regex to extract '--variable VAR_NAME=VALUE' or '--variable=VAR_NAME=VALUE' from raw CLI string
        const varRegex = new RegExp('--variable\\s+' + varName + '=["\']?([^"\'\\s]+)["\']?|--variable=' + varName + '=["\']?([^"\'\\s]+)["\']?');
        const match = cmdLine.match(varRegex);
        if (match) {
            cliVariables[varName] = match[1] || match[2];
        }
    });

    // 5. Update Package.swift inline in all locations
    packagePaths.forEach(packagePath => {
        if (!fs.existsSync(packagePath)) return;

        let content = fs.readFileSync(packagePath, 'utf8');
        let isModified = false;

        varNames.forEach(varName => {
            const targetValue = cliVariables[varName] || prefs[varName];
            if (!targetValue) return;

            // Matches initial placeholder with the comment target
            const placeholderRegex = new RegExp('\\$' + varName + '(?=.*\\/\\/\\s*cpm:' + varName + ')', 'g');

            // Matches any previously written exact: "version" with the comment target
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
