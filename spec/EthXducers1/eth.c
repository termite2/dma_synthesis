/*

typedef struct {
    u32  addr;
    u16  size;
    bool last;
    bool own;
} descr_t;

typedef struct {
    u32 addr;
    u16 size;
} frag_t;

int enqueue(descr_t * cb, int start, int end, frag_t * pkt, int nfrags) {
    for (int done = 1; done < nfrags; done++) {
        cb[(end + done) % QSIZE] = {pkt[done].addr, pkt[done].size, (done==nfrags-1), true};
    };
    cb[end] = {pkt[0].addr, pkt[0].size, (nfrags==1), true};
    return (end + nfrags) % QSIZE;
};

*/

typedef struct {
    u16  size;
    bool last;
    bool own;
} descr_t;



typedef struct {
    enum {
        END,     // bookmark
        DESCR,   // DMA descriptor
        DATA,    // data byte
        EOD,     // end-of-data -- follows the last byte of data
        FRAG,    // fragment descriptor
        EOP,     // end-of-packet -- follows the last fragment in a packet
        EOI,     // end-of-input -- always the last symbol in the input string
    } tag;
    typedef union {
        descr_t descr;
        u8      data;
    };
} csymbol;

typedef struct {
    enum {
        PKT,
        DATA,
        EOD,
        FRAG,
        EOP,
        EOI
    } tag;
    u8 data;
} asymbol;

/* abstractor transducer */

enum {
    
} abs_state_t;

void abstract (abs_state_t & state, const vector<csymbol> & in, vector<asymbol> & out) {
};

/* abstract append transducer */

enum {
} abs_append_state_t;

void abs_append (abs_append_state_t & state, const vector <asymbol> & in, vector<asymbol> & out) {
};

/* concrete append transducer */

enum {
} append_state_t;

void conc_append (appent_state_t & state, const vector <csymbol> & sym, vector<csymbol> & out) {

};
