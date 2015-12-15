This spec describes the receive path processing of the Broadcom NetXtreme family of devices.

/* Overview */

The driver allocates empty buffers for incoming packets and passes these buffers to the device
via a circular buffer, called "receive producer ring".  The device writes an incoming packet to 
the first available packet buffer in the CB and stores a completion notification in another CB, 
called receive return ring.  The driver reads completion notifications from the return ring and
forwards them to the OS in the form of "socket buffers" (a Linux data structure that describes
a network packet).

An interesting twist in this scheme is that there are actually two different receive producer 
rings in this device: one for regular-size frames and one for "jumbo" frames.  The two rings have
different descriptor formats, standard and extended receive descriptors respectively.  A standard
descriptor points to a contiguous data buffer.  An extended descriptor contains four address/length
pairs and can therefore point to up to 4 packet fragments.  When the device receives a packet, it 
places it into one of the two rings depending on its size.  However, there is only one queue for 
completion notifications.  Completion ring descriptors have the same format as standard rx 
descriptors.  A jumbo frame completion descriptor contains the total length of the received packet
in its length field (however, the actual packet data is scattered across up to 4 buffers).


The software (driver) part of the system is modeled by three transducers:

* Standard rx descriptor producer
* Extended rx descriptor producer
* Completion queue handler


            -----------------
  buffer    | std rx descr  | std rx descriptors
----------->| producer      |------------
            |               |           |             -------------------               -------------------
            -----------------           |             |                 | completion    | Completion queue| socket
                                        --------------| Device rx engine|---------------| handler         |---------
            -----------------           |             |                 | descriptors   |                 | buffers|
  buffer    | jumbo rx descr|           |             -------------------               -------------------        |    ------------------
----------->| producer      |-----------                       ^                                                   ---->|                |
            |               | ext rx descriptors               |------------------------------------------------------->|     Safety     |
            -----------------                                  |Ethernet                                                |                |
                                                               |frames                                                  ------------------

The two descriptor producers take a stream of memory buffers as inputs and produce rx descriptors as 
outputs.  The device rx engine transducer only accepts std or jumbo descriptor when it has a packet
arriving from the network.  Thus, the scheduling of the pipeline components is controller by arrival
of network packets, as expected.

We start with defining the input and output formats of each transducer.

/* Memory buffer */

A memory buffer contiguous in both virtual and physical address spaces.  It is 
represented by a buffer header symbol followed by 0 or more Byte symbols whose 
number matches the "size" field of the header:

Buffer{ vaddr: uint64_t            // virtual and physical addresses of the buffer
      , paddr: uint64_t
      , capacity: uint64_t         // buffer capacity
      , reserved: uint64_t         // number of bytes reserved at the start of the buffer.  These bytes will be left untouched by any subsequent processing.
}

/* Standard rx descriptor */

StdRxDescr{ flag_ip_csum:  bool                 // The 
          , flag_error:    bool                 // An error occurred when receiving the packet.  Error cause is encoded in err_XXX flags below
          , flag_vlan_tag: bool                 // Packet contains a vlan tag
          , ip_csum:       uint16_t             // Packet contains IP-level checksum
          , vlan_tag:      uint16_t             // vlan tag (only meaningful if flag_vlan_tag==true)
          , err_giant:     bool                 // Packet larger than maximal allowed size
          , err_less_64:   bool                 // Packet smaller than 64 bytes
          , err_crc:       bool                 // MAC-level CRC error
          , err_mac:       bool                 // Error occurred at the MAC level
          , opaque:        uint32_t             // Can be used by the driver; the device never reads or writes this field; the Linux driver sets 
                                                // this field to 1 for std descriptors and 2 for extended descriptor
          , buffer:        (length:uint16_t, Buffer)   // The driver writes available buffer capacity to length; 
                                                // the driver overwrites this value with the number of bytes actually read
}

/* Extended rx descriptor: same format as Std descriptor, except for the last field */
ExtRxDescr{ length:        uint16_t[4]
          , flag_ip_csum:  bool
          , flag_error:    bool
          , flag_vlan_tag: bool
          , ip_csum:       uint16_t
          , vlan_tag:      uint16_t
          , err_giant:     bool
          , err_less_64:   bool
          , err_crc:       bool
          , err_mac:       bool
          , opaque:        uint32_t
          , buffer:        (length:uint16_t, Buffer)[4] // The driver writes available buffer capacity to the length field of each buffer
                                                        // The device stores the entire packet size to the first length field and writes zero to the remaining ones
}

/* Ethernet frame */

Ethernet frame consists of a header followed by packet data represented by 0 or more NetByte symbols.  
If the error flag is 0 in the header, then there is no data.  Otherwise, there will be length NetBytes.

EthFrame{ is_ip:      bool       // is this an IP frame (and hence the IP checksum is present)
        , has_vlan:   bool       // frame has a VLAN tag
        , vlan_tag:   uint16_t   // tag value
        , length:     uint16_t   // payload length
        , csum_ok:    bool       // MAC-level checksum is correct
        , ipcsum_ok:  bool       // IP-level checksum
        , error:      bool       // MAC-level error occurred
}

/* Socket buffer */
SkBuff{ has_vlan: bool                                  // VLAN tag present
      , vlan_tag: uint16_t                              // VLAN tag value
      , nbuffers: uint16_t                              // number of buffers
      , buffers:  (length:uint16_t, Buffer)[nbuffers]   // packet buffers
}


/* Std rx descritptor producer */

See the StdRxProducer transducer

/* Extended rx descriptor producer */

See the ExtRxProducer transducer

/* Device */

See DevRxEngine transducer

/* Completion queue handler */

See the RxCompletionHandler transducer
