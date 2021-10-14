# use for deploy the function

# code path
wefe_code_path=$1

# upload files to nas
nas_upload(){
  # add config before nas init, and execute the first time it uses s tool
  s_config=`s config get -l`
  if [[ $s_config =~ 's-config' ]]
  then
    echo "already init s-config"
  else
    echo "does not init s-config, now start to init ..."
    s config add --AccessKeyID $access_key_id --AccessKeySecret  $access_key_secret --AccountID $account_id --aliasName s-config
  fi

  # init project
  s nas init

  # copy root, python, config.properties
  has_python=`s nas command ls  -l nas:///mnt/auto`
  if [[ $has_python =~ 'python' ]]
  then
    echo "has python, root environment."
  else
    root_dir=".s/build/artifacts/wefe-fc/index/.s/python"
    if [ ! -d $root_dir ]; then
      echo "local dir has no python, root environment, now run 's build --use-docker' command to download ..."
      s build --use-docker
    else
      echo "remote nas has no python, root environment, now upload to nas ..."
      s nas upload -r -n .s/build/artifacts/wefe-fc/index/.s/root nas:///mnt/auto/root --debug
      s nas upload -r -n .s/build/artifacts/wefe-fc/index/.s/python nas:///mnt/auto/python --debug
    fi

  fi

  # delete remote dir
  s nas command rm -rf /mnt/auto/$nas_env --debug

  # create env dir
  mkdir -p $nas_env/pythonCode
  s nas upload -r -n ./$nas_env nas:///mnt/auto/$nas_env --debug
  rm -rf $nas_env

  # cp common, kernel to build dir
  cd ../../../../../../
  mkdir build
  find ./common/ -name "*.py" | cpio -pdm ./build
  find ./kernel/ -name "*.py" | cpio -pdm ./build

  cd common/python/calculation/fc/function/wefe-fc
  s nas upload ../../../../../../config.properties nas:///mnt/auto/$nas_env/pythonCode/ --debug
  s nas upload -r -n ../../../../../../build nas:///mnt/auto/$nas_env/pythonCode --debug

  rm -rf ../../../../../../build

}


fc_deploy(){
  # function dir
  if [ ! ${wefe_code_path} ]; then
    echo "wefe_code_path is null"
    wefe_code_path=/opt/welab/wefe
  fi

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

  if [ ! ${account_id} ]; then
    echo "account_id is null"
  else
    sed -i "s|acs:ram::.*:role|acs:ram::${account_id}:role|" s.yaml
  fi

  if [ ! ${vpc_id} -o ${vpc_id} == "" ]; then
    echo "vpc_id is null"
    sed -i '11,14s/^/#/' s.yaml
  else
    echo "vpc_id is not null"
    sed -i '11,14s/^#*//' s.yaml
    sed -i "s|vpcId: .*|vpcId: ${vpc_id}|g" s.yaml
    sed -i "s|vswitchIds: .*|vswitchIds: [\"${v_switch_ids}\"]|g" s.yaml
    sed -i "s|securityGroupId: .*|securityGroupId: ${security_group_id}|g" s.yaml
  fi

  echo '2. upload the python environment and code to NAS ...'
  nas_upload

  echo '3. deploying function ...'
  cd $fc_dir
  # modify s.yaml
  sed -i "s|mnt/auto/pythonCode|mnt/auto/${nas_env}/pythonCode|" s.yaml

  #fun deploy -y
  s deploy all --use-local -y
  echo 'deploy completed !'
}


if_fc(){
  backend=$(grep -v "^#" ../../../../../../config.properties | grep "wefe.job.backend=*")
  backend=${backend##*=}

  if [ "$backend" == "FC" ]; then
    fc_deploy
  fi
}

if_fc





