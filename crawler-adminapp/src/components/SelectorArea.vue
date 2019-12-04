<template>
    <textarea 
        :readonly="readonly" 
        :name="name" 
        :id="name" 
        :value="formattedValue"
        class="form-control"
        :placeholder="placeholder"
        @input="onInput($event)"
    ></textarea>
</template>

<script>
export default {
    name : 'SelectorArea',
    props :  ['value', 'name', 'readonly', 'placeholder', 'id'],
    data() {
        return {
            
        };
    },
    methods: {
        onInput(event) {
            this.$emit('input', this.replaceAll(this.replaceAll(event.target.value, '(\r\n|\n|\r)', ';'), ';+', ';'));
        },
        replaceAll(text, search, replacement) {
            return text ? text.replace(new RegExp(search, 'g'), replacement) : '';
        }
    },
    computed: {
        formattedValue() {
            return this.replaceAll(this.value, ';', '\n');
        }
    }
};
</script>