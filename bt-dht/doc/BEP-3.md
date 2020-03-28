# BEP-3


* An ordinary web server
* A static 'metainfo' file
* A BitTorrent tracker
* An 'original' downloader
* The end user web browsers
* The end user downloaders

To start serving:
    
    1. Start running a tracker (or, more likely, have one running already).
    2. Start running an ordinary web server, such as apache, or have one already.
    3. Associate the extension .torrent with mimetype application/x-bittorrent on their web server (or have done so already).
    4. Generate a metainfo (.torrent) file using the complete file to be served and the URL of the tracker.
    5. Put the metainfo file on the web server.
    6. Link to the metainfo (.torrent) file from some other web page.
    7. Start a downloader which already has the complete file (the 'origin').

To start downloading:
    
    1. Install BitTorrent (or have done so already).
    2. Surf the web.
    3. Click on a link to a .torrent file.
    4. Select where to save the file locally, or select a partial download to resume.
    5. Wait for download to complete.
    6. Tell downloader to exit (it keeps uploading until this happens).

bencoding:

    
    1. Strings are length-prefixed base ten followed by a colon and the string. For example 4:spam corresponds to 'spam'.
    2. Integers are represented by an 'i' followed by the number in base 10 followed by an 'e'. For example i3e corresponds to 3 and i-3e corresponds to -3. Integers have no size limitation. i-0e is invalid. All encodings with a leading zero, such as i03e, are invalid, other than i0e, which of course corresponds to 0.
    3. Lists are encoded as an 'l' followed by their elements (also bencoded) followed by an 'e'. For example l4:spam4:eggse corresponds to ['spam', 'eggs'].
    4. Dictionaries are encoded as a 'd' followed by a list of alternating keys and their corresponding values followed by an 'e'. For example, d3:cow3:moo4:spam4:eggse corresponds to {'cow': 'moo', 'spam': 'eggs'} and d4:spaml1:a1:bee corresponds to {'spam': ['a', 'b']}. Keys must be strings and appear in sorted order (sorted as raw strings, not alphanumerics).

metainfo files:(bencoded dictionaries UTF-8)

    {
        'announce':{TYPE string},
        'info': 
            {
                'name':{TYPE string},
                'piece length':{TYPE integer},
                'pieces':{TYPE string},
                //below 2 only one show up
                'length':{TYPE integer},//single file only
                'files'://multiple files
                    [
                        {
                            'length':{TYPE integer},
                            'path':{TYPE list}
                        },
                        ...
                    ]
            }
    }

trackers
    
    GET requests
    {
        'info_hash':{TYPE string},
        'peer_id':{TYPE string},
        'ip':{TYPE string},
        'port':{TYPE integer},//6881...6889
        'uploaded':{TYPE integer},
        'downloaded':{TYPE integer},
        'left':{TYPE integer},
        'event':{TYPE string}//started completed stopped empty
    }
    
    response
    {
        //fail
        'failure reason':{TYPE string}
        //success
        'interval':{TYPE integer},
        'peers':
            [
                {
                    'peer id':{TYPE string},
                    'ip':{TYPE string},
                    'port':{TYPE integer}
                }
            ]
    }
    
peer protocol

    