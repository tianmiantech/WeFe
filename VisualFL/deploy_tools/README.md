
## Wefe VisualFL Deploy Toolkit

### Prerequisites

Too run VisualFL, following dependency or tools required:

- machine to install wefe_visualfl_deploy_tools:

    - python virtualenv with Python>=3
    
    - setup SSH password less login to machine(s) for deploy visualfl framework.

- machine(s) to deploy visualfl framework:
    
    - Python>=3.7(with pip)
    
    - an isolated directory (each directory will be deployed with a copy of code)


### Build package

```bash
cd VisualFL/deploy_tools/visualfl_deploy 
python visualfl_deploy/_build.py
cd ..
pyhon setup.py sdist
```
### upload `VisualFL/deploy_tools/dist/visualfl_deploy-1.0.tar.gz` to server.

### Deploy

1. install visualfl deploy toolkit

    ``` bash
    # ceate a python virtual envirement (recommanded) or use an exist one.
    cd {base_dir}
    python -m venv venv
    source venv/bin/activate
    python -m pip install -U pip && python -m pip install visualfl_deploy-1.0.tar.gz
    ```

2. generate deploy template
    
    1) standalone deployment
    ```bash
    wefe_visualfl_deploy template standalone
    ```
   2) cluster deployment
    ```bash
    wefe_visualfl_deploy template cluster
    ```
3. read comments in generated template `standalone_template.yaml` or `template.yaml` and modify as you want.

4. run deploy cmd
    
    1) standalone deployment
    ```bash
    wefe_visualfl_deploy deploy deploy --config standalone_template.yaml
    ```
   2) cluster deployment
    ```bash
    wefe_visualfl_deploy deploy deploy --config template.yaml
    ```

### Services start and stop 

Services could be start/stop with scripts in `VisualFL/script` or, use visualfl deploy toolkits:

1.standalone deployment
```bash
wefe_visualfl_deploy services all start standalone_template.yaml
```
1.cluster deployment
```bash
wefe_visualfl_deploy services all start template.yaml
```


### Run examples

Jobs could be submitted at each deployed machine with master service started. 

```bash
cd {base_dir}
source venv/bin/activate
export PYTHONPATH=$PYTHONPATH:{base_dir}/VisualFL
sh VisualFL/examples/paddle_clas/run.sh 127.0.0.1:10002
```
