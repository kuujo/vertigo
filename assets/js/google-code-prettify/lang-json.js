PR['registerLangHandler'](
    PR['createSimpleLexer'](
        [
         [PR['PR_PLAIN'],       /^\s+/, null, '\t\n\r '],
         [PR['PR_KEYWORD'],     /^(?:true|false|null)\b/, null, 'tfn'],
         [PR['PR_LITERAL'],     /^[+\-]?(?:0xX[0-9a-f]+|(?:\d+|\d*\.\d+)(?:e[+\-]?\d+)?)/, null, '+-0123456789.'],
         [PR['PR_PUNCTUATION'], /^[,:{}\[\]]+/, null, ',:{}[]']
        ],
        [
         // A string followed by a colon is additionally a key.
         ['lang-jsonkey',       /^(\"(?:[^\n\r\\\"]|\\\S)*\")\s*:/],
         [PR['PR_STRING'],      /^\"(?:[^\n\r\\\"]|\\\S)*\"/]
        ]),
    ['json']);

PR['registerLangHandler'](
    PR['createSimpleLexer'](
        [
         // Apply the classes str and key to the key.
         [PR['PR_STRING'] + ' key', /^[\s\S]*/, null, '"']
        ],
        [
        ]),
    ['jsonkey']);