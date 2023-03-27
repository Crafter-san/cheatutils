import { addComponent } from '/components/Loader.js'

function createComponent(template) {
    let args = {
        template: template,
        created() {
            this.refresh();
        },
        data() {
            return {
                mode: 'list',
                list: null,
                name: null,
                refs: null,
                script: null,
                showRefs: false
            };
        },
        methods: {
            add() {
                this.mode = 'add';
                this.script = {};
            },
            assign(script) {
                let self = this;
                axios.put(`/api/scripts-assign/${encodeURIComponent(script.name)}`, script.key).then(function (response) {
                    self.list.forEach(s => {
                        if (s != script && s.key == script.key) {
                            s.key = -1;
                        }
                    })
                });
            },
            edit(name) {
                let self = this;
                axios.get(`/api/scripts/${encodeURIComponent(name)}`).then(function (response) {
                    self.mode = 'edit';
                    self.name = name;
                    self.script = response.data;
                });
            },
            refresh() {
                let self = this;
                axios.get('/api/scripts').then(function (response) {
                    self.mode = 'list';
                    self.list = response.data;
                });
            },
            remove(name) {
                let self = this;
                axios.delete(`/api/scripts/${encodeURIComponent(name)}`).then(function (response) {
                    self.refresh();
                });
            },
            save() {
                let self = this;
                let handleError = error => {
                    alert(error.response.data);
                }
                if (this.mode == 'add') {
                    axios.post('/api/scripts', this.script).then(function (response) {
                       self.refresh();
                    }, handleError);
                }
                if (this.mode == 'edit') {
                    axios.put(`/api/scripts/${encodeURIComponent(this.name)}`, this.script).then(function (response) {
                        self.refresh();
                    }, handleError);
                }
            },
            showApiRef() {
                if (this.showRefs) {
                    this.showRefs = false;
                } else {
                    if (this.refs) {
                        this.showRefs = true;
                    } else {
                        let self = this;
                        axios.get('/api/scripts-doc/handle-keybindings').then(response => {
                            self.showRefs = true;
                            self.refs = response.data;
                        });
                    }
                }
            }
        }
    };
    addComponent(args, 'ScriptEditor');
    return args;
}

export { createComponent }