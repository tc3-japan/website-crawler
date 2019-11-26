/* eslint-disable */
import Vue from 'vue';
import VueI18n from 'vue-i18n';

import axios from 'axios';
import store from './store';

Vue.use(VueI18n);

const loadedLanguages = [ ];

let defaultLanguage = (process.env.VUE_APP_DEFAULT_LANGUAGE) ? process.env.VUE_APP_DEFAULT_LANGUAGE : 'en';

export const i18n = new VueI18n({
  locale: defaultLanguage,
  localeDir: 'lang',
  fallbackLocale: defaultLanguage
});

function setI18nLanguage (lang) {
  i18n.locale = lang;
  axios.defaults.headers.common['Accept-Language'] = lang;
  document.querySelector('html').setAttribute('lang', lang);
  return lang;
}

export function getLocale() {
    return i18n.locale;
}

export function loadLanguageAsync(lang) {

    if (lang !== store.state.language)
        store.commit('setLanguage', lang);

  // If the same language or the language was already loaded
  if (loadedLanguages.includes(lang)) {
    return Promise.resolve(setI18nLanguage(lang));
  }


  // If the language hasn't been loaded yet
  return import(/* webpackChunkName: "lang-[request]" */ `./lang/${lang}.js`).then(
    messages => {
      i18n.setLocaleMessage(lang, messages.default);
      loadedLanguages.push(lang);
      return setI18nLanguage(lang);
    }
  ).catch(e => {
    if (lang != defaultLanguage)
      return loadLanguageAsync(defaultLanguage);
  });
}

loadLanguageAsync(defaultLanguage);