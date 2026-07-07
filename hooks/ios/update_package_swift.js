#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    const { PluginInfoProvider } = context.requireCordovaModule('cordova-common');
    const pluginInfoProvider = new PluginInfoProvider();
    const pluginInfo = pluginInfoProvider.getPluginInfo(context.opts.plugin.dir);

    // Новое имя переменной
    const varName = 'IOS_FIREBASE_POD_VERSION';

    const cliVariables = context.opts.cli_variables || {};
    let targetValue = cliVariables[varName];

    if (!targetValue) {
        targetValue = pluginInfo.getPreferences(context.opts.platforms)[varName]
                      || pluginInfo.getPreferences()[varName];
    }

    if (!targetValue) {
        console.warn(`⚠️ Переменная ${varName} не найдена ни в CLI, ни в plugin.xml. Пропуск.`);
        return;
    }

    const packagePath = path.join(context.opts.plugin.dir, 'Package.swift');

    if (fs.existsSync(packagePath)) {
        let packageContent = fs.readFileSync(packagePath, 'utf8');

        // Новый плейсхолдер для поиска (экранируем знак $, так как это спецсимвол в RegExp)
        const searchValue = '\\$IOS_FIREBASE_POD_VERSION';

        if (packageContent.match(new RegExp(searchValue, 'g'))) {
            packageContent = packageContent.replace(new RegExp(searchValue, 'g'), targetValue);
            fs.writeFileSync(packagePath, packageContent, 'utf8');
            console.log(`✅ Package.swift успешно обновлен. Версия Firebase Pod: "${targetValue}"`);
        } else {
            console.warn(`⚠️ Плейсхолдер $IOS_FIREBASE_POD_VERSION не найден в Package.swift.`);
        }
    } else {
        console.warn('❌ Файл Package.swift не найден в директории плагина.');
    }
};
