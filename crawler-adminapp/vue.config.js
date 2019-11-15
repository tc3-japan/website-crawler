module.exports = {
    devServer: {
        proxy: 'http://ae91f8bcb014711ea9701063bddf76ed-904913025.ap-northeast-1.elb.amazonaws.com',
        disableHostCheck: true
    },
    // i18n: {
    //     locale: 'en',             // The locale of project localization
    //     fallbackLocale: 'en',     // The fallback locale of project localization
    //     localeDir: 'lang',        // The directory where store localization messages of project
    //     enableInSFC: false        // Enable locale messages in Single file components
    // }
};
