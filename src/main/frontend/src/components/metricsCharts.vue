<script>

    import { uuid } from './common.vue';

    const DEFAULT_TOOLTIP_OPTION = {
		trigger: 'axis',
		backgroundColor: 'rgba(255, 255, 255, 0.7)',
		borderColor: '#ccc',
		borderWidth: 1,
		textStyle: {
			color: '#666'
		}
	};

    export const distributionChart = {
        template: '<div :id="id" :data="data" style="width: 600px; height: 400px;"></div>',
        props: {
            id: {
                default: () => `chart-id-${uuid()}`
            },
            data: {
                default: {
                    title: '',
                    xData: [],
                    yData: []
                }
            }
        },
        watch: {
            data(data) {
                this.render(data);
            }
        },
        methods: {
            render(data) {
                this.$echarts.init(document.getElementById(this.id))
                    .setOption({
                        tooltip: Object.assign({
                            formatter(params) {
                                return [
                                    '时间段(ms): ' + params[0].name,
                                    '数量: ' + params[0].value
                                ].join('<br/>');
                            }
                        }, DEFAULT_TOOLTIP_OPTION),
                        toolbox: {
                            show: false
                        },
                        title: {
                            text: data.title,
                            x: 'center',
                            textStyle: {
                                fontSize: 14,
                                wordWrap: 'break-word'
                            }
                        },
                        grid: {},
                        xAxis: [{
                            name: '时间段\n(ms)',
                            type: 'category',
                            boundaryGap: true,
                            data: data.xData
                        }],
                        yAxis: [{
                            name: '数量',
                            type: 'value',
                            position: 'left',
                            scale: true
                        }],
                        series: [{
                            name : '数量',
                            type: 'bar',
                            animation: false,
                            data: data.yData
                        }]
                    });
            }
        }
    };

    export const requestChart = {
        template: '<div :id="id" :data="data" style="width: 600px; height: 400px;"></div>',
        props: {
            id: {
                default: () => `chart-id-${uuid()}`
            },
            data: {
                default: {
                    title: '',
                    xData: [],
                    yData: []
                }
            }
        },
        watch: {
            data(data) {
                this.render(data);
            }
        },
        methods: {
            render(data) {
                this.$echarts.init(document.getElementById(this.id))
                    .setOption({
                        tooltip: Object.assign({
                            formatter(params) {
                                return [
                                    '时间: ' + params[0].name,
                                    '数量: ' + params[0].value
                                ].join('<br/>');
                            }
                        }, DEFAULT_TOOLTIP_OPTION),
                        toolbox: {
                            show: false
                        },
                        title: {
                            text: data.title,
                            x: 'center',
                            textStyle: {
                                fontSize: 14,
                                wordWrap: 'break-word'
                            }
                        },
                        grid: {},
                        xAxis: [{
                            name: '时间',
                            type: 'category',
                            boundaryGap: true,
                            axisLabel: {
                                rotate: 30
                            },
                            data: data.xData
                        }],
                        yAxis: [{
                            name: '数量',
                            type: 'value',
                            position: 'left',
                            min: 0,
                            scale: true
                        }],
                        series: [{
                            name : '数量',
                            type: 'line',
                            animation: false,
                            data: data.yData
                        }]
                    });
            }
        }
    };

</script>