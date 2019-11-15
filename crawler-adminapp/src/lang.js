/* eslint-disable */
import Vue from 'vue';
import VueI18n from 'vue-i18n';
import messages from '@/lang/en';
import axios from 'axios';
import config from '../config';
import store from './store';

Vue.use(VueI18n);

let defaultLanguage = (config && config.client) ? config.client.defaultLanguage : 'en';

console.log('defaultLanguage', defaultLanguage);
console.log('messages', messages);

export const i18n = new VueI18n({
  locale: defaultLanguage,
  fallbackLocale: defaultLanguage
});

i18n.setLocaleMessage(defaultLanguage, messages);

const loadedLanguages = [ defaultLanguage ];

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

    console.log('setting lang', lang);
  // If the same language or the language was already loaded
  if (loadedLanguages.includes(lang)) {
    return Promise.resolve(setI18nLanguage(lang));
  }

  console.log('loading lang', lang);
  // If the language hasn't been loaded yet
  return import(/* webpackChunkName: "lang-[request]" */ `./lang/${lang}.js`).then(
    messages => {
      i18n.setLocaleMessage(lang, messages.default);
      loadedLanguages.push(lang);
      return setI18nLanguage(lang);
    }
  );
}