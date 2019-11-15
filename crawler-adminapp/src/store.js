import Vue from 'vue';
import Vuex from 'vuex';
import VuexPersistence from 'vuex-persist';

Vue.use(Vuex);

const vuexLocal = new VuexPersistence({
    storage: window.localStorage,
    reducer: (state) => ({
        language : state.language,
        token : state.token
    }),
});

export default new Vuex.Store({
  state: {
    language: '',
  },
  mutations: {
    setLanguage(state, lang) {
      state.language = lang;
    },
    setToken(state, token) {
      state.token = token;
    },
  },
  actions: {},
  getters: {
    isAuthenticated: state => state.token !== null
  },
  plugins: [vuexLocal.plugin]
});