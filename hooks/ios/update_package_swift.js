#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    // 1. Enhanced safe check for iOS platform
    const projectRoot = context.opts.projectRoot || path.resolve(__dirname, '../../');
    const iosPlatformPath = path.join(projectRoot, 'platforms', 'ios');

    const hasIosPlatform = context.opts && context.opts.platforms && context.opts.platforms.includes('ios');
    const hasIosDirectory = fs.existsSync(iosPlatformPath);

    // If it's not an iOS-targeted build/install and the iOS directory doesn't exist yet, skip execution
    if (!hasIosPlatform && !hasIosDirectory) {
        return;
    }

    // 2. Import cordova-common using standard Node.js require for Cordova 13+
    const cordovaCommon = require('cordova-common');
    const PluginInfoProvider = cordovaCommon.PluginInfoProvider;

    let pluginInfo;
    try {
        const provider = new PluginInfoProvider();
        pluginInfo = provider.get(context.opts.plugin.dir);
    } catch (e) {
        console.error('❌ Failed to initialize PluginInfoProvider:', e);
        return;
    }

    // Target variable name
    const varName = 'IOS_FIREBASE_POD_VERSION';

    // 3. Resolve the value: CLI variable takes precedence over plugin.xml default preference
    const cliVariables = context.opts.cli_variables || {};
    let targetValue = cliVariables[varName];

    if (!targetValue && pluginInfo) {
        const platformPrefs = pluginInfo.getPreferences('ios') || {};
        const globalPrefs = pluginInfo.getPreferences() || {};
        targetValue = platformPrefs[varName] || globalPrefs[varName];
    }

    if (!targetValue) {
        console.warn(`⚠️ Variable ${varName} not found in CLI variables or plugin.xml preferences. Skipping.`);
        return;
    }

    // 4. Resolve path to Package.swift inside the plugin directory and replace the placeholder
    const packagePath = path.join(context.opts.plugin.dir, 'Package.swift');

    if (fs.existsSync(packagePath)) {
        let packageContent = fs.readFileSync(packagePath, 'utf8');

        // Escape the $ sign for the RegExp matching
        const searchValue = '\\$IOS_FIREBASE_POD_VERSION';

        if (packageContent.match(new RegExp(searchValue, 'g'))) {
            packageContent = packageContent.replace(new RegExp(searchValue, 'g'), targetValue);
            fs.writeFileSync(packagePath, packageContent, 'utf8');
            console.log(`✅ Package.swift successfully updated with version: "${targetValue}"`);
        } else {
            console.warn(`⚠️ Placeholder $IOS_FIREBASE_POD_VERSION not found in Package.swift.`);
        }
    } else {
        console.warn('❌ Package.swift file not found in the plugin directory.');
    }
};
