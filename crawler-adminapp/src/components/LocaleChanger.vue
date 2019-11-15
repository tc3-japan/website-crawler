<template>
    <div class="locale-changer">
        <b-input-group size="sm">
          <b-input-group-prepend is-text>
              <i class="pe-7s-global mr-2" style="font-size: 1.4em;" /> {{$t('LANGUAGE')}} 
          </b-input-group-prepend>
          <b-form-select v-model="locale" :options="langs"></b-form-select>
        </b-input-group>
    </div>
</template>

<script>
import { loadLanguageAsync, getLocale } from '../lang';

export default {
  name: 'locale-changer',
  data () {
    return { 
        langs: [ 
          { value : 'ja', text : '日本語' }, 
          { value : 'en', text : 'English' }
        ],
        locale : ''
    };
  },
  mounted() {
    this.locale = this.$store.state.language;
  },
  watch: {
    locale: function (current, previous) {
      loadLanguageAsync(current);
    }
  }
};
</script>

<style scoped>
.locale-changer .input-group-text {
    background-color: #6b6979;
    border-color: #a6a4b7;
    color: #bcbad0;
}
</style>