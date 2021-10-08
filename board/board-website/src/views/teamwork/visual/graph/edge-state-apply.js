/* inherit node */

export default G6 => {
    G6.registerEdge(
        'flow-edge',
        {
            stateApplying(name, value, item) {
                const group = item.getContainer();
                const children = group.getChildren();
                const policy = {
                    selected() {
                        children[0].attr({
                            lineDash: value ? [4, 2, 1, 1] : null,
                        });
                    },
                };

                policy[name] && policy[name]();
            },
        },
        'cubic-edge',
    );
};
