function createComponent(template) {
    return {
        template: template,
        created() {
            let self = this;
            axios.get('/api/container-buttons').then(function (response) {
                self.config = response.data;
            });
        },
        data() {
            return {
                config: null
            };
        },
        methods: {
            update() {
                let self = this;
                axios.post('/api/container-buttons', this.config).then(function (response) {
                    self.config = response.data;
                });
            }
        }
    }
}

export { createComponent }