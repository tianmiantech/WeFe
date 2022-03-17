# use for deploy the function

# code path
wefe_code_path=$1
# function dir
if [ ! ${wefe_code_path} ]; then
  echo "wefe_code_path is null"
  wefe_code_path=/opt/welab/wefe
fi

# upload files to nas
nas_upload(){

  s config add --AccessKeyID $access_key_id --AccessKeySecret  $access_key_secret --AccountID $account_id --aliasName s-config

  # init project
  s nas init

  # delete remote dir
  s nas command rm -rf /mnt/auto/$nas_env/pythonCode --debug

  # create env dir
  mkdir -p $nas_env/pythonCode
  has_env=`s nas command ls  -l nas:///mnt/auto`
  if [[ $has_env == $nas_env ]]
  then
    echo 'has env dir, upload pythonCode ...'
    s nas upload -r -o ./$nas_env/pythonCode /mnt/auto/$nas_env/ --debug
  else
    echo 'has not env dir, upload env dir ...'
    s nas upload -r -o ./$nas_env /mnt/auto/ --debug
  fi
  rm -rf $nas_env

  # copy root, python, config.properties
  has_python=`s nas command ls  -l nas:///mnt/auto/$nas_env`
  if [[ $has_python == 'python' ]]
  then
    echo "has python, root environment."
  else
    root_dir="/data/environment/.s/python"
    if [ ! -d $root_dir ]; then
      echo "local dir has no python, root environment, now run 's build --use-docker' command to download ..."
      s build --use-docker --debug
      echo 'copy new python, root to path: /data/environment/.s/ '
      cp -rf .s/build/artifacts/wefe-fc/index/.s/python /data/environment/.s/
      cp -rf .s/build/artifacts/wefe-fc/index/.s/root /data/environment/.s/
    fi

    echo 'upload new python to NAS ...'
    s nas upload -r -o /data/environment/.s/python /mnt/auto/$nas_env --debug
    s nas upload -r -o /data/environment/.s/root /mnt/auto/$nas_env --debug
  fi

  # cp common, kernel to build dir
  cd ../../../../../../
  mkdir build
  find ./common/ -name "*.py" | cpio -pdm ./build
  find ./kernel/ -name "*.py" | cpio -pdm ./build

  cd common/python/calculation/fc/function/wefe-fc
  s nas upload -r -o ../../../../../../config.properties nas:///mnt/auto/$nas_env/pythonCode/  --debug
  s nas upload -r -o ../../../../../../build/ /mnt/auto/$nas_env/pythonCode  --debug

  rm -rf ../../../../../../build

}

fc_deploy(){

  fc_dir=${wefe_code_path}/common/python/calculation/fc/function/wefe-fc
  cd $fc_dir

  export PYTHONPATH=$wefe_code_path
  source /data/environment/miniconda3/envs/wefe-python37/bin/activate

  echo "reading configure from config.properties"

  nas_env=$(grep -v "^#" ../../../../../../config.properties | grep "env.name=*")
  nas_env=${nas_env##*=}

  access_key_id=$(grep -v "^#" ../../../../../../config.properties | grep "fc.access_key_id=*")
  access_key_id=${access_key_id##*=}

  access_key_secret=$(grep -v "^#" ../../../../../../config.properties | grep "fc.access_key_secret=*")
  access_key_secret=${access_key_secret##*=}

  account_id=$(grep -v "^#" ../../../../../../config.properties | grep "fc.account_id=*")
  account_id=${account_id##*=}

  vpc_id=$(grep -v "^#" ../../../../../../config.properties | grep "fc.vpc_id=*")
  vpc_id=${vpc_id##*=}

  v_switch_ids=$(grep -v "^#" ../../../../../../config.properties | grep "fc.v_switch_ids=*")
  v_switch_ids=${v_switch_ids##*=}

  security_group_id=$(grep -v "^#" ../../../../../../config.properties | grep "fc.security_group_id=*")
  security_group_id=${security_group_id##*=}

  account_type=$(grep -v "^#" ../../../../../../config.properties | grep "fc.account_type=*")
  account_type=${account_type##*=}

  if [ ! ${account_id} ]; then
    echo "account_id is null"
  else
    sed -i "s|acs:ram::.*:role|acs:ram::${account_id}:role|" s.yaml
  fi

  if [ ! ${nas_env} ]; then
    echo "nas env is null"
  else
    sed -i "s|fc-env:.*|fc-env: ${nas_env}|" s.yaml
  fi

  if [[ ${account_type,,} == "admin" ]]; then
    echo "account_type is admin, auto to create fc role"
    sed -i '9s/^/#/' s.yaml
  elif [[ ${account_type,,} == "api" ]]; then
    echo "account_type is api, create fc role manually"
    sed -i '9s/^#*//' s.yaml
  else
    echo "not support type: ${account_type}, please check again !"
  fi

  if [[ ! $vpc_id ]] || [[ $vpc_id == "" ]]; then
    echo "vpc_id is null"
    sed -i  '11,14s/^/#/' s.yaml
  else
    echo "vpc_id is not null"
    sed -i  '11,14s/^#*//' s.yaml
    sed -i  "s|vpcId: .*|vpcId: ${vpc_id}|g" s.yaml
    sed -i  "s|vswitchIds: .*|vswitchIds: [\"${v_switch_ids}\"]|g" s.yaml
    sed -i  "s|securityGroupId: .*|securityGroupId: ${security_group_id}|g" s.yaml
  fi

  echo '2. upload the python environment and code to NAS ...'
  nas_upload

  echo '3. deploying function ...'
  cd $fc_dir

  #fun deploy -y
  s deploy all --use-local -y
  echo 'deploy completed !'
}

fc_deploy





