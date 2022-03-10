cd ../ || exit

action_info=$(cat "test/case1.json")
sh main.sh "/Users/zane/Code/WeLab/Wefe/config.properties" $action_info