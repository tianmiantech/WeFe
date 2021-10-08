#!/bin/bash
# nas shell

# fat、dev、prod
nas_env=$1

# access key id
access_key_id=$3

# access key secret
access_key_secret=$4

# account id
account_id=$5


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
  echo "no python, root environment, now upload to nas ..."
  s nas upload -r -n .fun/root nas:///mnt/auto/root --debug
  s nas upload -r -n .fun/python nas:///mnt/auto/python --debug
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







