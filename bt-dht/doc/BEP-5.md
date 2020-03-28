# BEP-5

## DHT
    based on Kademila and over UDP
    
## term explanation
    peer 
    node
    node id space : random from 160-bit space
    distance metric : XOR and interpreted as unsigned integer, smaller are closer 

## encode format

### bencode : text -> encode_byte_stream    
    text := string | integer | list | dictionary
    string := 
    integer :=
    list :=
    dictionary :=
    
    encode rule : 
        string -> [length]:<string>
        integer -> i<integer>e
        list -> l<>e
        dictionary -> d<>e  ;;key is String and order by alpha sequence

### compack info of peer contact encode : text -> encode_byte_stream
    text := ip & port
    
    ip -> ip_network_byte_order
    port -> port_network_byte_order

### compack info of node contact encode : text -> encode_byte_stream
    text := nodeId & ip & port
    
    nodeId -> nodeId_network_byte_order
    ip -> ip_network_byte_order
    port -> port_network_byte_order

## routing table

### bucket
hold K(normally 8) nodes
when full divided in half
last changed: When a node in a bucket is pinged and it responds, or a node is added to a bucket, or a node in a bucket is replaced with another node
Buckets not changed in 15 minutes should be "refreshed." picking a random ID in the range of the bucket and performing a find_nodes search on it
property
    
    range
    last changed


### node
property
   
    nodeId
    state : good | questionable | bad
        good : query and response within 15 minutes, a query from node within 15 minutes will refresh the good state
        questionable : after 15 minutes of inactivity
        bad : fail to respond to multiple query
    last changed

   
KRPC Protocol
    
    three message type
    1. query
    2. response
    3. error
    
    query type
    1. ping
    2. find_node
    3. get_peers
    4. announce_peer
    
    message format
    {
        't':{TYPE string},
        'y':'q' or 'r' or 'e',
        'v':{TYPE string},
        //if 'y' == 'q'
        'q':{TYPE string},
        'a':
            {
                'id':{TYPE string}
            },
        
        ,
        //if 'y' == 'r'
        'r':
            {
                'id':{TYPE string}
            },
        //if 'y' == 'e'
        'e':[{TYPE integer},{TYPE string}]
    }

Contact Encoding

    1. Compack IP/Port info
        6-byte String = 4 byte for ip + 2 byte for port
    2. Compact nodeId info
        26-byte String = 20 byte for id + 6 byte for ip/port
## step
1. BENCODER
2. KRPC
3. DHT PROTOCOL


# 翻译
为了存储trackerless种子的peer的联系信息，BitTorrent 使用了DHT技术。
从而使得每一个peer都成为了服务器。这个协议是在Kademila的基础上在UDP端口实现的。

路由表
每一个node维护一个存储了已知good node的路由表。路由表中的nodes成为DHT发起查询的起点。路由表中的node做为结果返回给来自其他nodes的请求。

我们所获取到的结点不是平等的。一些是“好”的，一些不是。依靠DHT，许多的nodes能够发出查询请求和接受回复，但却不能够回复某些nodes的查询。


## TODO
- [x] bencoder
- [x] krpc
- [x] routing table
- [ ] 

