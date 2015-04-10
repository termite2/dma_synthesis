// Transducer state enumeration
type XState;
const unique XS1, XS2, XS3 : XState;

var xState: XState;

// Safety automaton state enumeration
type AState;
const unique AS1, AS2 : AState;

var aState: AState;

// input symbol types
type XInput;
const unique a, b, startb, xeoi : XInput;

type AInput;
const unique c, aeoi : AInput;

// cnt_a, cnt_b, cnt_c variables
var cnt_a : int;
var cnt_b : int;
var cnt_c : int;

// MAX threshold
const MAX : int;
axiom MAX > 0;

// Transducer transition procedure
procedure deltaT(i : XInput, n : int) returns ()
modifies cnt_a, cnt_b, cnt_c, xState, aState;
{
    if (xState == XS1 && i == a) {
        assume (cnt_a > 0);
        cnt_a := cnt_a - 1;
        call deltaA(c);
    } else if (xState == XS1 && i == startb) {
        if (cnt_a < n) {
            call deltaA(aeoi);
            xState := XS3;
        } else {
            cnt_b := n;
            xState := XS2;
        }
    } else if (xState == XS2 && i == b) {
        assume (cnt_b > 0);
        cnt_b := cnt_b - 1;
        call deltaA(c);
    } else if (xState == XS2 && i == xeoi) {
        assume (cnt_b == 0);
        xState := XS3;
        call deltaA(aeoi);
    }
}


// Automaton transition function
procedure deltaA(i : AInput) returns ()
modifies cnt_c, aState;
{
    if (aState == AS1 && i == c) {
        cnt_c := cnt_c - 1;
        assert (cnt_c >= 0);
    } else if (aState == AS1 && i == aeoi) {
        aState := AS2;
    }
}

procedure main() returns(res:int) 
modifies cnt_a, cnt_b, cnt_c, xState, aState;
{
    var i : XInput;
    var n : int;

    cnt_a := MAX;
    cnt_c := MAX;
    xState := XS1;
    aState := AS1;

    while (*) {
        havoc i;
        havoc n;
        call deltaT(i,n);
    }
    res := 0;
    return;
}
