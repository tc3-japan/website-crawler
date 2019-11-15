<template>
    <div class="app-footer">
        <div class="app-footer__inner">
            <div class="app-footer-left">
                Copyright 2019 DashboardPack.com
            </div>
            <div class="app-footer-right">
                <locale-changer  />
            </div>
        </div>
    </div>
</template>

<script>
    import LocaleChanger from './LocaleChanger';

    export default {
        name: "Footer",
        components: {
            LocaleChanger
        }
    };
</script>
