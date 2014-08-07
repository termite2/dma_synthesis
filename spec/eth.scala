// Simplified Ethernet controller DMA layout model

/*
 * Abstract type: packet queue 
 */

// A packet fragment is an (address, size) tuple
type Fragment    = Tuple2[Int, Int]

// A packet consists of 1 or more fragments
type Packet      = List[Fragment]

type PacketQueue = List[Packet]

/* Abstract operations */

// Enqueue a packet
def absPush (q : PacketQueue, p : Packet) : PacketQueue = q :+ p

/* 
 * Concrete type: flattened list of buffer descriptors 
 */

// DMA descriptor stores a single packet fragment, as a three-tuple (address, size, last), 
// where _last_ is true for the last fragment of a packet, and _own_ is true if the descriptor
// is owned by the device.
type Descr = Tuple4[Int, Int, Boolean, Boolean]

// DMA queue is a list of descriptors.
type DMAQueue = List[Descr]

/* Concrete mutators */

def setDescr (q : DMAQueue, i : Int, d : Descr) : DMAQueue = q.updated(i, d)
def appendDescr (q : DMAQueue, d : Descr) : DMAQueue = q :+ d

/*
 * Semantic function: maps a concrete instance to an abstract one
 */

def sem(q : DMAQueue) : PacketQueue = do_sem(q, Nil)

// Scan the input descriptor list until reaching either the end of 
// the list or a descriptor with own flag set to false.
def do_sem(q : List[Descr], frags : List[Fragment]) : PacketQueue = {
    q match {
        case Nil => // end of list
            assert (frags.isEmpty) // must not be in the middle of a packet
            Nil
        case (addr, sz, lst, own) :: rest => 
            val pkt = frags :+ (addr, sz)
            (lst, own) match {
                case (true, true)  => pkt :: do_sem(rest, Nil)
                case (false, true) => do_sem(rest, pkt)
                case _             => assert (frags.isEmpty) // descriptor not owned by the device
                                      Nil
            }
    }
}

/*
 * Method to be synthesised
 */
def concPush (q : DMAQueue, p : Packet) : DMAQueue = 
{
    require(!p.isEmpty);
    choose {
        (res : DMAQueue) => absPush (sem(q), p) == sem(res)
    }
}


/*
 * Expected synthesised implementation
 */
//def concPush (q : DMAQueue, p : Packet) : DMAQueue = {
//    require(!p.isEmpty);
//    val q1 = q :+ (p._1, p._2, , false)
//    p.foreach(frag => );
//} 
//ensuring {(res : DMAQueue) => absPush (sem(q), p) = sem(res) }
