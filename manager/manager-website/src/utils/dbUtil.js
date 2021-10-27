
class DBUtil {
    db = null
    dbname
    keyPath   // db key
    table     // db table
    indexedDB = null
    constructor(options = {
        dbname:  'wefe-chat',
        table:   'accountId',
        keyPath: 'time',
    }) {
        this.dbname = options.dbname;
        this.keyPath = options.keyPath;
        this.table = options.table;
        this.indexedDB = window.indexedDB || window.webkitIndexedDB || window.mozIndexedDB || window.msIndexedDB;

        return this.openDB();
    }

    openDB () {
        return new Promise((resolve, reject) => {
            if (this.indexedDB) {
                const connect = this.indexedDB.open(this.dbname);

                connect.onsuccess = (e) => {
                    this.db = connect.result;
                    console.log('数据库连接成功!');
                    resolve(this);
                };
                connect.onerror = (e) => {
                    this.onerror(e);
                };
                // update
                connect.onupgradeneeded = (e) => {
                    this.onupgradeneeded(e);
                };

            } else {
                reject('indexedDB is not supported!');
            }
        });
    }

    onerror (event) {
        console.log('数据库连接失败');
    }

    onupgradeneeded (event) {
        console.log('更新数据库...');
        this.db = event.target.result;
        // create table
        if (!this.db.objectStoreNames.contains(this.table)) {
            this.db.createObjectStore(this.table, { keyPath: this.keyPath });
        }
    }

    addData (data) {
        const connect = this.db.transaction([this.table], 'readwrite').objectStore(this.table).add(data);

        return new Promise((resolve, reject) => {
            connect.onsuccess = () => {
                console.log('数据写入成功');
                resolve();
            };
            connect.onerror = (event) => {
                reject(event.target.error.message);
            };
        });
    }

    deleteData (key) {
        const transaction = this.db.transaction([this.table], 'readwrite');
        const objectStore = transaction.objectStore(this.table);
        const connect = objectStore.delete(key);

        return new Promise((resolve, reject) => {
            connect.onsuccess = () => {
                resolve();
            };
            connect.onerror = (event) => {
                reject(event.target.error.message);
            };
        });
    }

    putData (key, data) {
        const transaction = this.db.transaction([this.table]);
        const objectStore = transaction.objectStore(this.table);
        const connect = objectStore.put({ id: key, ...data });

        return new Promise((resolve, reject) => {
            connect.onsuccess = () => {
                resolve();
            };
            connect.onerror = (event) => {
                reject(event.target.error.message);
            };
        });
    }

    getData (key) {
        const transaction = this.db.transaction([this.table]);
        const objectStore = transaction.objectStore(this.table);
        const connect = objectStore.get(key);

        return new Promise((resolve, reject) => {

            connect.onerror = () => {
                reject(null);
            };

            connect.onsuccess = () => {
                resolve(connect.result);
            };
        });
    }

    readAll () {
        const objectStore = this.db.transaction(this.table).objectStore(this.table);

        return new Promise(resolve => {
            const all = [];

            objectStore.openCursor().onsuccess = (event) => {
                const cursor = event.target.result;

                if (cursor) {
                    all.push(cursor.value);
                    cursor.continue();
                } else {
                    resolve(all);
                }
            };
        });
    }

    close () {
        this.db && this.db.close();
    }
}

export default DBUtil;
