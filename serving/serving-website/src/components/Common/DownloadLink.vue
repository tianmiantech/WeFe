<template>
    <div
        :style="{display: `${inline ? 'inline-block' : 'block'}`}"
        @click="goDownLoad"
    >
        <slot />
    </div>
</template>

<script>
    import { isQianKun } from '@src/http/utils';
    import { baseURL, getTokenName } from '@src/utils/constant';
    import { getToken, createUUID, formatDate } from '@tianmiantech/util';

    export default {
        props: {
            midUrl: {
                type:    String,
                default: '',
            },
            inline: {
                type:    Boolean,
                default: false,
            },
        },
        methods: {
            goDownLoad() {
                let downloadUrl = `${baseURL()}${this.midUrl}`;

                if (isQianKun()) {
                    downloadUrl += `${downloadUrl.indexOf('?') < 0 ? '?' : '&'}x-user-token=${getToken(getTokenName())}&`;
                    downloadUrl += `x-req-rd=${createUUID()}&`;
                    downloadUrl += `x-req-rd=${formatDate()}`;
                }
                window.open(downloadUrl);
            },
        },
    };
</script>
