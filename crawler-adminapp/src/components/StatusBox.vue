<template>
    <b-alert 
        show 
        dismissible
        :variant="type">
            {{message}}
    </b-alert>
</template>


<script>
export default {
    name : 'StatusBox',
    props: {
        type : {
            required : false,
            type: String
        },
        message : {
            required : false,
            type: String
        }
    }
}
</script>