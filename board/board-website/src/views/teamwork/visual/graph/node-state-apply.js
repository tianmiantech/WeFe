/* inherit edge */

function animate (icon) {
    if (!icon.cfg.animating) {
        let deg = 0, limit = false;

        // add running animate
        icon.animate(radio => {
            if (limit) {
                deg -= 0.001;
                if (deg <= 0.1) {
                    limit = false;
                }
            } else {
                deg += 0.001;
                if (deg >= 0.2) {
                    limit = true;
                }
            }
            icon.rotateAtPoint(-70, 0, deg);
        },
        {
            repeat:   true,
            duration: 600000,
            easing:   'easeCubic',
        });
    }
}

function changeNodeState (model, run_status, group, children) {
    const stateIcon = group.$getItem('flow-node-stateIcon');

    model.style.stroke = '#1890ff';
    model.style.fill = '#ecf3ff';
    model.labelCfg.fill = '#4483FF';
    if (run_status !== 'default') {
        model.stateIcon = {
            y:          10,
            x:          -81,
            show:       true,
            fontFamily: 'iconfont',
            cursor:     'pointer',
            fontSize:   20,
        };
    }

    switch (run_status) {
        case 'created':
            model.stateIcon = {
                ...model.stateIcon,
                text:  '\ue614',
                style: {
                    stroke: '#fff',
                    fill:   '#909399',
                },
            };
            model.style.fill = '#ecf3ff';
            model.style.stroke = '#909399';
            model.labelCfg.fill = '#909399';
            break;
        case 'wait':
            model.stateIcon = {
                ...model.stateIcon,
                text:  '\ue614',
                style: {
                    stroke: '#fff',
                    fill:   '#909399',
                },
            };
            model.style.fill = '#ecf3ff';
            model.style.stroke = '#909399';
            model.labelCfg.fill = '#909399';
            break;
        case 'running':
            model.stateIcon = {
                ...model.stateIcon,
                text:  '\ue634',
                style: {
                    stroke: '#fff',
                    fill:   '#ff9900',
                },
            };
            model.style.fill = '#fffbf1';
            model.style.stroke = '#ff9900';
            model.labelCfg.fill = '#ff9900';
            break;
        case 'success':
            model.stateIcon = {
                ...model.stateIcon,
                text:  '\ue624',
                style: {
                    stroke: '#fff',
                    fill:   '#67C23A',
                },
            };
            model.style.stroke = '#67C23A';
            model.style.fill = '#f2f9ec';
            model.labelCfg.fill = '#49b83e';
            break;
        case 'stop':
            model.stateIcon = {
                ...model.stateIcon,
                text:  '\ue604',
                style: {
                    stroke: '#fff',
                    fill:   '#F56C6C',
                },
            };
            model.style.stroke = '#F56C6C';
            model.style.fill = '#FCf0f0';
            model.labelCfg.fill = '#F56C6C';
            break;
        case 'failed':
            model.stateIcon = {
                ...model.stateIcon,
                text:  '\ue62d',
                style: {
                    stroke: '#fff',
                    fill:   '#F56C6C',
                },
            };
            model.style.stroke = '#F56C6C';
            model.style.fill = '#FCf0f0';
            model.labelCfg.fill = '#F56C6C';
            break;
        case 'restart':
            model.stateIcon = {
                ...model.stateIcon,
                text:  '\ue625',
                style: {
                    stroke: '#fff',
                    fill:   '#E6A23C',
                },
            };
            model.labelCfg.fill = '#E6A23C';
            break;
    }

    if (!stateIcon) {
        // add state icon
        this.drawIcon({}, group, model);
    } else {
        // update the icon
        if (run_status === 'running') {
            stateIcon.attr({
                text: model.stateIcon.text,
                ...model.stateIcon.style,
            });
        } else {
            stateIcon.stopAnimate();
            stateIcon.remove();
            this.drawIcon({}, group, model);
        }
    }

    const icon = group.$getItem('flow-node-stateIcon');

    if (icon) {
        if (run_status === 'running') {
            animate(icon);
        } else {
            icon.stopAnimate();
            // TODO
            icon.rotateAtPoint(-70, 0, 0);
        }
    }

    // update node styles
    children[0].attr({
        ...model.style,
    });
    children[1].attr({
        ...model.labelCfg,
    });
}

export default G6 => {
    G6.registerNode('flow-node', {
        shapeType: 'rect',
        draw (cfg, group) {
            return this.drawShape(cfg, group);
        },
        stateApplying (name, value, item) {
            const _this = this;
            const model = item.getModel();
            const group = item.getContainer();
            const children = group.getChildren();
            const styles = item.get('styles');
            const policy = {
                selected () {
                    children[0].attr({
                        lineWidth: value ? 2 : 1,
                        lineDash:  value ? [4, 2, 4, 2] : null,
                        cursor:    value ? 'move' : '',
                    });
                },
                nodeStatus () {
                    changeNodeState.call(_this, model, value, group, children);
                },
                highlight () {
                    if (styles.highlight) {
                        children[0].attr({
                            ...styles.highlight,
                        });
                        children[1].attr({
                            ...styles.highlight.labelCfg.style,
                        });
                    }
                },
            };

            policy[name] && policy[name]();
        },
    }, 'rect-node');
};
