<script>

    import { uuid } from './common.vue';

    export const metricsTable = {
        template: '<Table :id="id" :data="data" :columns="columns" height="600"></Table>',
        props: {
            id: {
                default: () => `chart-id-${uuid()}`
            },
            data: {
                default: []
            }
        },
        data() {
            return {
                columns: [{
                    key: 'urlPattern',
                    title: 'url',
                    sortable: true
                }, {
                    key: 'cnt',
                    title: '总数',
                    width: 100,
                    sortable: true
                }, {
                    key: 'max',
                    title: 'max (ms)',
                    width: 110,
                    sortable: true
                }, {
                    key: 'min',
                    title: 'min (ms)',
                    width: 110,
                    sortable: true
                }, {
                    key: 'avg',
                    title: 'avg (ms)',
                    width: 110,
                    sortable: true
                }, {
                    key: 'action',
                    title: '操作',
                    width: 150,
                    render: (h, params) => {
                        return h('div', [
                            h('Button', {
                                props: {
                                    type: 'text',
                                    size: 'small',
                                },
                                style: {
                                    'margin-left': '-20px'
                                }
                            }, '请求分布'),
                            h('Button', {
                                props: {
                                    type: 'text',
                                    size: 'small',
                                },
                                style: {
                                    'margin-left': '-10px'
                                },
                                on: {
                                    click: () => {
                                        this.$emit('on-show-distribution', params);
                                    }
                                }
                            }, '耗时分布')
                        ]);
                    }
                }]
            }
        }
    };

</script>