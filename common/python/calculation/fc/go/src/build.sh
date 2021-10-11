echo '准备下载依赖...'
go mod download
echo '准备编译...'
go build -buildmode=c-shared -o ../pkg/utils.so utils.go
