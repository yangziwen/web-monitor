<style scoped>
</style>
<template>
    <Date-picker type="daterange" placeholder="选择时间段"
        :value="range" format="yyyy-MM-dd"
        :options="options"
        style="width: 200px; display: inline-block;"
        @on-change="onChange"></Date-picker>
</template>
<script>
    export default {
        props: ['initRange'],
        data () {
            const $this = this;
            return {
                range: $this.initRange,
                options: {
                    shortcuts: [
                        {
                            text: '前一天',
                            value() {
                                return [
                                    $this.$moment().subtract(1, 'd').format('YYYY-MM-DD'),
                                    $this.$moment().subtract(1, 'd').format('YYYY-MM-DD'),
                                ];
                            }
                        },
                        {
                            text: '最近一周',
                            value () {
                                return [
                                    $this.$moment().subtract(7, 'd').format('YYYY-MM-DD'),
                                    $this.$moment().subtract(1, 'd').format('YYYY-MM-DD')
                                ];
                            }
                        },
                        {
                            text: '最近一个月',
                            value () {
                                return [
                                    $this.$moment().subtract(1, 'M').format('YYYY-MM-DD'),
                                    $this.$moment().subtract(1, 'd').format('YYYY-MM-DD')
                                ];
                            }
                        },
                        {
                            text: '最近三个月',
                            value () {
                                return [
                                    $this.$moment().subtract(3, 'M').format('YYYY-MM-DD'),
                                    $this.$moment().subtract(1, 'd').format('YYYY-MM-DD'),
                                ];
                            }
                        }
                    ]
                }
            }
        },
        methods: {
            onChange(range) {
                this.$emit('on-change', range);
            }
        }
    };
</script>