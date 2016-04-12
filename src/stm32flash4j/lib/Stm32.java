package stm32flash4j.lib;

/**
 * Created by Alfa on 11.4.2016 г..
 */

//All addresses used in STM32 programming are lower than 0x80000000, so int would suffice
//TODO revise short and byte usage
import java.io.PrintStream;

import static stm32flash4j.lib.Stm32.stm32_err_t.*;
import static stm32flash4j.lib.Stm32.port_err_t.*;


public class Stm32 {

    static PrintStream stderr;

    static void usleep(long micro_s){
        //TODO
    };
    static stm32_t stm;

    //main



    static boolean is_addr_in_ram(int addr)
    {
        return addr >= stm.dev.ram_start && addr < stm.dev.ram_end;
    }

    static boolean is_addr_in_flash(int addr)
    {
        return addr >= stm.dev.fl_start && addr < stm.dev.fl_end;
    }

    /* returns the page that contains address "addr" */
    static int flash_addr_to_page_floor(int addr)
    {
        int page;
        int psize[];

        if (!is_addr_in_flash(addr))
            return 0;

        page = 0;
        addr -= stm.dev.fl_start;
        psize = stm.dev.fl_ps;

        int i = 0;
        while (addr >= psize[i]) {
            addr -= psize[i];
            page++;
            if (psize[i + 1] != 0)
                i++;
        }

        return page;
    }

    /* returns the first page whose start addr is >= "addr" */
    static int flash_addr_to_page_ceil(int addr)
    {
        int page;
        int psize[];

        if (!(addr >= stm.dev.fl_start && addr <= stm.dev.fl_end))
        return 0;

        page = 0;
        addr -= stm.dev.fl_start;
        psize = stm.dev.fl_ps;

        int i = 0;
        while (addr >= psize[i]) {
            addr -= psize[i];
            page++;
            if (psize[i + 1] != 0)
                i++;
        }

        return addr != 0 ? page + 1 : page;
    }

    /* returns the lower address of flash page "page" */
    static int flash_page_to_addr(int page)
    {
        int i,j;
        int addr, psize[];

        addr = stm.dev.fl_start;
        psize = stm.dev.fl_ps;

        i = 0;
        for (j = 0; j < page; j++) {
            addr += psize[i + 0];
            if (psize[i+1] != 0)
                i++;
        }

        return addr;
    }    
    
    
    
    
    //#include "utils.h"

    /* detect CPU endian */
    static boolean cpu_le() {
        int cpu_le_test = 0x12345678;
        return (byte)cpu_le_test == 0x78;
    }

    static int be_u32(int v) {
        if (cpu_le())
            return	((v & 0xFF000000) >> 24) |
                    ((v & 0x00FF0000) >>  8) |
                    ((v & 0x0000FF00) <<  8) |
                    ((v & 0x000000FF) << 24);
        return v;
    }

//    static int le_u32(int v) {
//        if (!cpu_le())
//            return  ((v & 0xFF000000) >> 24) |
//                    ((v & 0x00FF0000) >>  8) |
//                    ((v & 0x0000FF00) <<  8) |
//                    ((v & 0x000000FF) << 24);
//        return v;
//    }

    static byte[] le_u32(int value) {
        if (!cpu_le()) {
            return new byte[] {
                    (byte)(value >>> 24),
                    (byte)(value >>> 16),
                    (byte)(value >>> 8),
                    (byte)value};
        }else {
            return new byte[] {
                    (byte)value,
                    (byte)(value >>> 8),
                    (byte)(value >>> 16),
                    (byte)(value >>> 24)};
        }

    }

    //#define _H_PORT

    /* flags */
    public static final int  PORT_BYTE	=(1 << 0);	/* byte (not frame) oriented */
    public static final int  PORT_GVR_ETX	=(1 << 1);	/* cmd GVR returns protection status */
    public static final int  PORT_CMD_INIT	=(1 << 2);	/* use INIT cmd to autodetect speed */
    public static final int  PORT_RETRY	=(1 << 3);	/* allowed read() retry after timeout */
    public static final int  PORT_STRETCH_W	=(1 << 4);	/* warning for no-stretching commands */

     enum port_err_t{
        PORT_ERR_OK, //= 0,
                PORT_ERR_NODEV,		/* No such device */
                PORT_ERR_TIMEDOUT,	/* Operation timed out */
                PORT_ERR_UNKNOWN,
    } ;


    /*
     * Specify the length of reply for command GET
     * This is helpful for frame-oriented protocols, e.g. i2c, to avoid time
     * consuming try-fail-timeout-retry operation.
     * On byte-oriented protocols, i.e. UART, this information would be skipped
     * after read the first byte, so not needed.
     */
    static class varlen_cmd {
        byte version;
        byte length;
    };

    interface port_interface{
        //String name = null;
        int flags();

//        port_err_t open(port_options *ops);
        port_err_t close();
        port_err_t read(byte []buf, int nbyte);
        port_err_t read(byte []buf, int nbyte, int start);
        port_err_t write(byte []buf, int nbyte);
        port_err_t write(byte []buf, int nbyte, int start);
//        port_err_t gpio(serial_gpio_t n, int level);
//        String (*get_cfg_str)(port_interface port);
        varlen_cmd[] cmd_get_reply();
//        void *private;
    };



    public static final int F_NO_ME = 1 << 0;	/* Mass-Erase not supported */
    public static final int F_OBLL  = 1 << 1;	/* OBL_LAUNCH required */

//devices


    public static final int SZ_128	= 0x00000080;
    public static final int SZ_256	= 0x00000100;
    public static final int SZ_1K	= 0x00000400;
    public static final int SZ_2K	= 0x00000800;
    public static final int SZ_16K	= 0x00004000;
    public static final int SZ_32K	= 0x00008000;
    public static final int SZ_64K	= 0x00010000;
    public static final int SZ_128K	= 0x00020000;
    public static final int SZ_256K	= 0x00040000;

/*
 * Page-size for page-by-page flash erase.
 * Arrays are zero terminated; last non-zero value is automatically repeated
 */

    /* fixed size pages */
    static int p_128[] = { SZ_128, 0 };
    static int p_256[] = { SZ_256, 0 };
    static int p_1k[]  = { SZ_1K,  0 };
    static int p_2k[]  = { SZ_2K,  0 };
    /* F2 and F4 page size */
    static int f2f4[]  = { SZ_16K, SZ_16K, SZ_16K, SZ_16K, SZ_64K, SZ_128K, 0 };
    /* F4 dual bank page size */
    static int f4db[]  = {
            SZ_16K, SZ_16K, SZ_16K, SZ_16K, SZ_64K, SZ_128K, SZ_128K, SZ_128K,
            SZ_16K, SZ_16K, SZ_16K, SZ_16K, SZ_64K, SZ_128K, 0
    };
    /* F7 page size */
    static int f7[]    = { SZ_32K, SZ_32K, SZ_32K, SZ_32K, SZ_128K, SZ_256K, 0 };

/*
 * Device table, corresponds to the "Bootloader device-dependant parameters"
 * table in ST document AN2606.
 * Note that the option bytes upper range is inclusive!
 */
    static stm32_dev_t devices[] = {
	/* ID   "name"                                                              SRAM-address-range      FLASH-address-range    PPS      PSize   Option-byte-addr-range  System-mem-addr-range   Flags */
	/* F0 */
            new stm32_dev_t((short)0x440, "STM32F030x8/F05xxx"              , 0x20000800, 0x20002000, 0x08000000, 0x08010000,  (short)4, p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFEC00, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x442, "STM32F030xC/F09xxx"              , 0x20001800, 0x20008000, 0x08000000, 0x08040000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, F_OBLL),
            new stm32_dev_t((short)0x444, "STM32F03xx4/6"                   , 0x20000800, 0x20001000, 0x08000000, 0x08008000,  (short)4, p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFEC00, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x445, "STM32F04xxx/F070x6"              , 0x20001800, 0x20001800, 0x08000000, 0x08008000,  (short)4, p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFC400, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x448, "STM32F070xB/F071xx/F72xx"        , 0x20001800, 0x20004000, 0x08000000, 0x08020000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFC800, 0x1FFFF800, 0),
	/* F1 */
            new stm32_dev_t((short)0x412, "STM32F10xxx Low-density"         , 0x20000200, 0x20002800, 0x08000000, 0x08008000,  (short)4, p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x410, "STM32F10xxx Medium-density"      , 0x20000200, 0x20005000, 0x08000000, 0x08020000,  (short)4, p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x414, "STM32F10xxx High-density"        , 0x20000200, 0x20010000, 0x08000000, 0x08080000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x420, "STM32F10xxx Medium-density VL"   , 0x20000200, 0x20002000, 0x08000000, 0x08020000,  (short)4, p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x428, "STM32F10xxx High-density VL"     , 0x20000200, 0x20008000, 0x08000000, 0x08080000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x418, "STM32F105xx/F107xx"              , 0x20001000, 0x20010000, 0x08000000, 0x08040000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFB000, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x430, "STM32F10xxx XL-density"          , 0x20000800, 0x20018000, 0x08000000, 0x08100000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFE000, 0x1FFFF800, 0),
	/* F2 */
            new stm32_dev_t((short)0x411, "STM32F2xxxx"                     , 0x20002000, 0x20020000, 0x08000000, 0x08100000,  (short)1, f2f4  , 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
	/* F3 */
            new stm32_dev_t((short)0x432, "STM32F373xx/F378xx"              , 0x20001400, 0x20008000, 0x08000000, 0x08040000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x422, "STM32F302xB(C)/F303xB(C)/F358xx" , 0x20001400, 0x2000A000, 0x08000000, 0x08040000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x439, "STM32F301xx/F302x4(6/8)/F318xx"  , 0x20001800, 0x20004000, 0x08000000, 0x08010000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x438, "STM32F303x4(6/8)/F334xx/F328xx"  , 0x20001800, 0x20003000, 0x08000000, 0x08010000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x446, "STM32F302xD(E)/F303xD(E)/F398xx" , 0x20001800, 0x20010000, 0x08000000, 0x08080000,  (short)2, p_2k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFD800, 0x1FFFF800, 0),
	/* F4 */
            new stm32_dev_t((short)0x413, "STM32F40xxx/41xxx"               , 0x20003000, 0x20020000, 0x08000000, 0x08100000,  (short)1, f2f4  , 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new stm32_dev_t((short)0x419, "STM32F42xxx/43xxx"               , 0x20003000, 0x20030000, 0x08000000, 0x08200000,  (short)1, f4db  , 0x1FFEC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new stm32_dev_t((short)0x423, "STM32F401xB(C)"                  , 0x20003000, 0x20010000, 0x08000000, 0x08040000,  (short)1, f2f4  , 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new stm32_dev_t((short)0x433, "STM32F401xD(E)"                  , 0x20003000, 0x20018000, 0x08000000, 0x08080000,  (short)1, f2f4  , 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new stm32_dev_t((short)0x458, "STM32F410xx"                     , 0x20003000, 0x20008000, 0x08000000, 0x08020000,  (short)1, f2f4  , 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new stm32_dev_t((short)0x431, "STM32F411xx"                     , 0x20003000, 0x20020000, 0x08000000, 0x08080000,  (short)1, f2f4  , 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new stm32_dev_t((short)0x421, "STM32F446xx"                     , 0x20003000, 0x20020000, 0x08000000, 0x08080000,  (short)1, f2f4  , 0x1FFFC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
            new stm32_dev_t((short)0x434, "STM32F469xx"                     , 0x20003000, 0x20060000, 0x08000000, 0x08200000,  (short)1, f4db  , 0x1FFEC000, 0x1FFFC00F, 0x1FFF0000, 0x1FFF7800, 0),
	/* F7 */
            new stm32_dev_t((short)0x449, "STM32F74xxx/75xxx"               , 0x20004000, 0x20050000, 0x08000000, 0x08100000,  (short)1, f7    , 0x1FFF0000, 0x1FFF001F, 0x1FF00000, 0x1FF0EDC0, 0),
	/* L0 */
            new stm32_dev_t((short)0x425, "STM32L031xx/041xx"               , 0x20001000, 0x20002000, 0x08000000, 0x08008000, (short)32, p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, 0),
            new stm32_dev_t((short)0x417, "STM32L05xxx/06xxx"               , 0x20001000, 0x20002000, 0x08000000, 0x08010000, (short)32, p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, 0),
            new stm32_dev_t((short)0x447, "STM32L07xxx/08xxx"               , 0x20002000, 0x20005000, 0x08000000, 0x08030000, (short)32, p_128 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF02000, 0),
	/* L1 */
            new stm32_dev_t((short)0x416, "STM32L1xxx6(8/B)"                , 0x20000800, 0x20004000, 0x08000000, 0x08020000, (short)16, p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, F_NO_ME),
            new stm32_dev_t((short)0x429, "STM32L1xxx6(8/B)A"               , 0x20001000, 0x20008000, 0x08000000, 0x08020000, (short)16, p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF01000, 0),
            new stm32_dev_t((short)0x427, "STM32L1xxxC"                     , 0x20001000, 0x20008000, 0x08000000, 0x08040000, (short)16, p_256 , 0x1FF80000, 0x1FF8001F, 0x1FF00000, 0x1FF02000, 0),
            new stm32_dev_t((short)0x436, "STM32L1xxxD"                     , 0x20001000, 0x2000C000, 0x08000000, 0x08060000, (short)16, p_256 , 0x1FF80000, 0x1FF8009F, 0x1FF00000, 0x1FF02000, 0),
            new stm32_dev_t((short)0x437, "STM32L1xxxE"                     , 0x20001000, 0x20014000, 0x08000000, 0x08080000, (short)16, p_256 , 0x1FF80000, 0x1FF8009F, 0x1FF00000, 0x1FF02000, F_NO_ME),
	/* L4 */
            new stm32_dev_t((short)0x415, "STM32L476xx/486xx"               , 0x20003100, 0x20018000, 0x08000000, 0x08100000,  (short)1, p_2k  , 0x1FFF7800, 0x1FFFF80F, 0x1FFF0000, 0x1FFF7000, 0),
	/* These are not (yet) in AN2606: */
            new stm32_dev_t((short)0x641, "Medium_Density PL"               , 0x20000200, 0x20005000, 0x08000000, 0x08020000,  (short)4, p_1k  , 0x1FFFF800, 0x1FFFF80F, 0x1FFFF000, 0x1FFFF800, 0),
            new stm32_dev_t((short)0x9a8, "STM32W-128K"                     , 0x20000200, 0x20002000, 0x08000000, 0x08020000,  (short)4, p_1k  , 0x08040800, 0x0804080F, 0x08040000, 0x08040800, 0),
            new stm32_dev_t((short)0x9b0, "STM32W-256K"                     , 0x20000200, 0x20004000, 0x08000000, 0x08040000,  (short)4, p_2k  , 0x08040800, 0x0804080F, 0x08040000, 0x08040800, 0),
            //{0x0}
    };




    /*
  stm32flash - Open Source ST STM32 flash program for *nix
  Copyright (C) 2010 Geoffrey McRae <geoff@spacevs.com>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

//
//
//    #include <stdint.h>
//    #include "serial.h"

            public static final int STM32_MAX_RX_FRAME	=256;	/* cmd read memory */
            public static final int STM32_MAX_TX_FRAME=	(1 + 256 + 1);	/* cmd write memory */

    public static final int STM32_MAX_PAGES	=	0x0000ffff;
            public static final int STM32_MASS_ERASE	=0x00100000; /* > 2 x max_pages */

    public enum stm32_err_t {
        STM32_ERR_OK /*= 0*/,
                STM32_ERR_UNKNOWN,	/* Generic error */
                STM32_ERR_NACK,
                STM32_ERR_NO_CMD,	/* Command not available in bootloader */
    } ;


//    private static enum flags_t{
//        F_NO_ME = 1 << 0,	/* Mass-Erase not supported */
//                F_OBLL  = 1 << 1,	/* OBL_LAUNCH required */
//    } ;


    private static class stm32_t {
        //const serial_t		*serial;
        port_interface	port;
        byte			bl_version;
        byte			version;
        byte			option1, option2;
        short		pid;
        stm32_cmd_t	cmd;
        stm32_dev_t	dev;
    };

    private static class stm32_dev_t {

        public stm32_dev_t(short id, String name, int ram_start, int ram_end, int fl_start, int fl_end, short fl_pps, int[] fl_ps, int opt_start, int opt_end, int mem_start, int mem_end, int flags) {
            this.id = id;
            this.name = name;
            this.ram_start = ram_start;
            this.ram_end = ram_end;
            this.fl_start = fl_start;
            this.fl_end = fl_end;
            this.fl_pps = fl_pps;
            this.fl_ps = fl_ps;
            this.opt_start = opt_start;
            this.opt_end = opt_end;
            this.mem_start = mem_start;
            this.mem_end = mem_end;
            this.flags = flags;
        }

        short	id;
        String	name;
        int	ram_start, ram_end;
        int	fl_start, fl_end;
        short	fl_pps; // pages per sector
        int	fl_ps[];  // page size
        int	opt_start, opt_end;
        int	mem_start, mem_end;
        int	flags;
    };
//
//    stm32_t *stm32_init(port_interface port, String init);
//    void stm32_close(stm32_t *stm);
//    stm32_err_t stm32_read_memory(stm32_t stm, int address,
//                                  byte data[], /*unsigned*/ int len);
//    stm32_err_t stm32_write_memory(stm32_t stm, int address,
//                                   const byte data[], /*unsigned*/ int len);
//    stm32_err_t stm32_wunprot_memory(stm32_t stm);
//    stm32_err_t stm32_wprot_memory(stm32_t stm);
//    stm32_err_t stm32_erase_memory(stm32_t stm, int spage,
//                                   int pages);
//    stm32_err_t stm32_go(stm32_t stm, int address);
//    stm32_err_t stm32_reset_device(stm32_t stm);
//    stm32_err_t stm32_readprot_memory(stm32_t stm);
//    stm32_err_t stm32_runprot_memory(stm32_t stm);
//    stm32_err_t stm32_crc_memory(stm32_t stm, int address,
//                                 int length, int *crc);
//    stm32_err_t stm32_crc_wrapper(stm32_t stm, int address,
//                                  int length, int *crc);
//    int stm32_sw_crc(int crc, byte *buf, /*unsigned*/ int len);
//

    
    
    
    
/*
  stm32flash - Open Source ST STM32 flash program for *nix
  Copyright 2010 Geoffrey McRae <geoff@spacevs.com>
  Copyright 2012-2014 Tormod Volden <debian.tormod@gmail.com>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
    

//
//    #include <stdlib.h>
//    #include <stdint.h>
//    #include <stdio.h>
//    #include <string.h>
//    #include <time.h>
//    #include <unistd.h>
//
//    #include "stm32.h"
//            #include "port.h"
//            #include "utils.h"
//
            public static final int STM32_ACK	= 0x79;
            public static final int STM32_NACK	= 0x1F;
            public static final int STM32_BUSY	= 0x76;

            public static final byte STM32_CMD_INIT	= 0x7F;
            public static final byte STM32_CMD_GET	= 0x00;	/* get the version and command supported */
            public static final byte STM32_CMD_GVR	= 0x01;	/* get version and read protection status */
            public static final byte STM32_CMD_GID	= 0x02;	/* get ID */
            public static final byte STM32_CMD_RM	= 0x11;	/* read memory */
            public static final byte STM32_CMD_GO	= 0x21;	/* go */
            public static final byte STM32_CMD_WM	= 0x31;	/* write memory */
            public static final byte STM32_CMD_WM_NS	= 0x32;	/* no-stretch write memory */
            public static final byte STM32_CMD_ER	= 0x43;	/* erase */
            public static final byte STM32_CMD_EE	= 0x44;	/* extended erase */
            public static final byte STM32_CMD_EE_NS	= 0x45;	/* extended erase no-stretch */
            public static final byte STM32_CMD_WP	= 0x63;	/* write protect */
            public static final byte STM32_CMD_WP_NS	= 0x64;	/* write protect no-stretch */
            public static final byte STM32_CMD_UW	= 0x73;	/* write unprotect */
            public static final byte STM32_CMD_UW_NS	= 0x74;	/* write unprotect no-stretch */
            public static final byte STM32_CMD_RP	= (byte)0x82;	/* readout protect */
            public static final byte STM32_CMD_RP_NS	= (byte)0x83;	/* readout protect no-stretch */
            public static final byte STM32_CMD_UR	= (byte)0x92;	/* readout unprotect */
            public static final byte STM32_CMD_UR_NS	= (byte)0x93;	/* readout unprotect no-stretch */
            public static final byte STM32_CMD_CRC	= (byte)0xA1;	/* compute CRC */
            public static final byte STM32_CMD_ERR	= (byte)0xFF;	/* not a valid command */

            public static final int STM32_RESYNC_TIMEOUT	= 35;	/* seconds */
            public static final int STM32_MASSERASE_TIMEOUT	= 35;	/* seconds */
            public static final int STM32_PAGEERASE_TIMEOUT	= 5;	/* seconds */
            public static final int STM32_BLKWRITE_TIMEOUT	= 1;	/* seconds */
            public static final int STM32_WUNPROT_TIMEOUT	= 1;	/* seconds */
            public static final int STM32_WPROT_TIMEOUT	= 1;	/* seconds */
            public static final int STM32_RPROT_TIMEOUT	= 1;	/* seconds */

            public static final int STM32_CMD_GET_LENGTH	= 17;	/* bytes in the reply */

    private static class stm32_cmd_t {
        byte get = STM32_CMD_ERR;
        byte gvr = STM32_CMD_ERR;
        byte gid = STM32_CMD_ERR;
        byte rm = STM32_CMD_ERR;
        byte go = STM32_CMD_ERR;
        byte wm = STM32_CMD_ERR;
        byte er = STM32_CMD_ERR; /* this may be extended erase */
        byte wp = STM32_CMD_ERR;
        byte uw = STM32_CMD_ERR;
        byte rp = STM32_CMD_ERR;
        byte ur = STM32_CMD_ERR;
        byte crc = STM32_CMD_ERR;
    }

/* Reset code for ARMv7-M (Cortex-M3) and ARMv6-M (Cortex-M0)
 * see ARMv7-M or ARMv6-M Architecture Reference Manual (table B3-8)
 * or "The definitive guide to the ARM Cortex-M3", section 14.4.
 */
    static byte stm_reset_code[] = {
            0x01, 0x49,		// ldr     r1, [pc, #4] ; (<AIRCR_OFFSET>)
            0x02, 0x4A,		// ldr     r2, [pc, #8] ; (<AIRCR_RESET_VALUE>)
            0x0A, 0x60,		// str     r2, [r1, #0]
        (byte)0xfe, (byte)0xe7,		// endless: b endless
            0x0c, (byte)0xed, 0x00, (byte)0xe0,	// .word 0xe000ed0c <AIRCR_OFFSET> = NVIC AIRCR register address
            0x04, 0x00, (byte)0xfa, 0x05	// .word 0x05fa0004 <AIRCR_RESET_VALUE> = VECTKEY | SYSRESETREQ
    };

    static int stm_reset_code_length = stm_reset_code.length;

/* RM0360, Empty check
 * On STM32F070x6 and STM32F030xC devices only, internal empty check flag is
 * implemented to allow easy programming of the virgin devices by the boot loader. This flag is
 * used when BOOT0 pin is defining Main Flash memory as the target boot space. When the
 * flag is set, the device is considered as empty and System memory (boot loader) is selected
 * instead of the Main Flash as a boot space to allow user to program the Flash memory.
 * This flag is updated only during Option bytes loading: it is set when the content of the
 * address 0x08000 0000 is read as 0xFFFF FFFF, otherwise it is cleared. It means a power
 * on or setting of OBL_LAUNCH bit in FLASH_CR register is needed to clear this flag after
 * programming of a virgin device to execute user code after System reset.
 */
    static byte stm_obl_launch_code[] = {
            0x01, 0x49,		// ldr     r1, [pc, #4] ; (<FLASH_CR>)
            0x02, 0x4A,		// ldr     r2, [pc, #8] ; (<OBL_LAUNCH>)
            0x0A, 0x60,		// str     r2, [r1, #0]
        (byte)0xfe, (byte)0xe7,		// endless: b endless
            0x10, 0x20, 0x02, 0x40, // address: FLASH_CR = 40022010
            0x00, 0x20, 0x00, 0x00  // value: OBL_LAUNCH = 00002000
    };

    static int stm_obl_launch_code_length = stm_obl_launch_code.length;
//
//    static stm32_dev_t devices[];
//
//    int flash_addr_to_page_ceil(int addr);
//
    static void stm32_warn_stretching(String f)
    {
        stderr.printf("Attention !!!\n");
        stderr.printf("\tThis %s error could be caused by your I2C\n", f);
        stderr.printf("\tcontroller not accepting \"clock stretching\"\n");
        stderr.printf("\tas required by bootloader.\n");
        stderr.printf("\tCheck \"I2C.txt\" in stm32flash source code.\n");
    }

    static stm32_err_t stm32_get_ack_timeout(stm32_t stm, long timeout)
    {
        port_interface port = stm.port;
        byte b[] = new byte[1];
        port_err_t p_err;
        long t0 = 0, t1;

        if ((port.flags() & PORT_RETRY) == 0)
            timeout = 0;

        if (timeout > 0)
            t0 = System.currentTimeMillis();

        do {
            p_err = port.read(b, 1);
            if (p_err == PORT_ERR_TIMEDOUT && timeout > 0) {
                t1 = System.currentTimeMillis();
                if (t1 < t0 + timeout)
                    continue;
            }

            if (p_err != PORT_ERR_OK) {
                stderr.printf("Failed to read ACK byte\n");
                return STM32_ERR_UNKNOWN;
            }

            if (b[0] == STM32_ACK)
            return STM32_ERR_OK;
            if (b[0] == STM32_NACK)
            return STM32_ERR_NACK;
            if (b[0] != STM32_BUSY) {
                stderr.printf("Got byte 0x%02x instead of ACK\n", b);
                return STM32_ERR_UNKNOWN;
            }
        } while (true);
    }

    static stm32_err_t stm32_get_ack(stm32_t stm)
    {
        return stm32_get_ack_timeout(stm, 0);
    }

    static stm32_err_t stm32_send_command_timeout(stm32_t stm,
                                                  byte cmd,
                                                  long timeout)
    {
        port_interface port = stm.port;
        stm32_err_t s_err;
        port_err_t p_err;
        byte buf[] = new byte[2];

        buf[0] = cmd;
        buf[1] = (byte)(cmd ^ (byte)0xFF);
        p_err = port.write(buf, 2);
        if (p_err != PORT_ERR_OK) {
            stderr.printf("Failed to send command\n");
            return STM32_ERR_UNKNOWN;
        }
        s_err = stm32_get_ack_timeout(stm, timeout);
        if (s_err == STM32_ERR_OK)
            return STM32_ERR_OK;
        if (s_err == STM32_ERR_NACK)
            stderr.printf("Got NACK from device on command 0x%02x\n", cmd);
        else
            stderr.printf("Unexpected reply from device on command 0x%02x\n", cmd);
        return STM32_ERR_UNKNOWN;
    }

    static stm32_err_t stm32_send_command(stm32_t stm, byte cmd)
    {
        return stm32_send_command_timeout(stm, cmd, 0);
    }

    /* if we have lost sync, send a wrong command and expect a NACK */
    static stm32_err_t stm32_resync(stm32_t stm)
    {
        port_interface port = stm.port;
        port_err_t p_err;
        byte buf[] = new byte[2], ack[] = new byte[1];
        long t0, t1;

        t0 = System.currentTimeMillis();
        t1 = t0;

        buf[0] = STM32_CMD_ERR;
        buf[1] = STM32_CMD_ERR ^ (byte)0xFF;
        while (t1 < t0 + STM32_RESYNC_TIMEOUT) {
            p_err = port.write(buf, 2);
            if (p_err != PORT_ERR_OK) {
                usleep(500000);
                t1 = System.currentTimeMillis();
                continue;
            }
            p_err = port.read(ack, 1);
            if (p_err != PORT_ERR_OK) {
                t1 = System.currentTimeMillis();
                continue;
            }
            if (ack[0] == STM32_NACK)
                return STM32_ERR_OK;
            t1 = System.currentTimeMillis();
        }
        return STM32_ERR_UNKNOWN;
    }

    /*
     * some command receive reply frame with variable length, and length is
     * embedded in reply frame itself.
     * We can guess the length, but if we guess wrong the protocol gets out
     * of sync.
     * Use resync for frame oriented interfaces (e.g. I2C) and byte-by-byte
     * read for byte oriented interfaces (e.g. UART).
     *
     * to run safely, data buffer should be allocated for 256+1 bytes
     *
     * len is value of the first byte in the frame.
     */
    static stm32_err_t stm32_guess_len_cmd(stm32_t stm, byte cmd,
                                           byte []data, /*unsigned*/ int len)
    {
        port_interface port = stm.port;
        port_err_t p_err;

        if (stm32_send_command(stm, cmd) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;
        if ((port.flags() & PORT_BYTE) != 0) {
		/* interface is UART-like */
            p_err = port.read(data, 1);
            if (p_err != PORT_ERR_OK)
                return STM32_ERR_UNKNOWN;
            len = data[0];
            p_err = port.read(data, len + 1, 1);
            if (p_err != PORT_ERR_OK)
                return STM32_ERR_UNKNOWN;
            return STM32_ERR_OK;
        }

        p_err = port.read(data, len + 2);
        if (p_err == PORT_ERR_OK && len == data[0])
            return STM32_ERR_OK;
        if (p_err != PORT_ERR_OK) {
		/* restart with only one byte */
            if (stm32_resync(stm) != STM32_ERR_OK)
                return STM32_ERR_UNKNOWN;
            if (stm32_send_command(stm, cmd) != STM32_ERR_OK)
                return STM32_ERR_UNKNOWN;
            p_err = port.read(data, 1);
            if (p_err != PORT_ERR_OK)
                return STM32_ERR_UNKNOWN;
        }

        stderr.printf("Re sync (len = %d)\n", data[0]);
        if (stm32_resync(stm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        len = data[0];
        if (stm32_send_command(stm, cmd) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;
        p_err = port.read(data, len + 2);
        if (p_err != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;
        return STM32_ERR_OK;
    }

    /*
     * Some interface, e.g. UART, requires a specific init sequence to let STM32
     * autodetect the interface speed.
     * The sequence is only required one time after reset.
     * stm32flash has command line flag "-c" to prevent sending the init sequence
     * in case it was already sent before.
     * User can easily forget adding "-c". In this case the bootloader would
     * interpret the init sequence as part of a command message, then waiting for
     * the rest of the message blocking the interface.
     * This function sends the init sequence and, in case of timeout, recovers
     * the interface.
     */
    static stm32_err_t stm32_send_init_seq(stm32_t stm)
    {
        port_interface port = stm.port;
        port_err_t p_err;
        byte b[] = new byte[1], cmd[] = new byte[]{STM32_CMD_INIT};

        p_err = port.write(cmd, 1);
        if (p_err != PORT_ERR_OK) {
            stderr.printf("Failed to send init to device\n");
            return STM32_ERR_UNKNOWN;
        }
        p_err = port.read(b, 1);
        if (p_err == PORT_ERR_OK && b[0] == STM32_ACK)
        return STM32_ERR_OK;
        if (p_err == PORT_ERR_OK && b[0] == STM32_NACK) {
		/* We could get error later, but let's continue, for now. */
        stderr.printf("Warning: the interface was not closed properly.\n");
        return STM32_ERR_OK;
        }
        if (p_err != PORT_ERR_TIMEDOUT) {
            stderr.printf("Failed to init device.\n");
            return STM32_ERR_UNKNOWN;
        }

	/*
	 * Check if previous STM32_CMD_INIT was taken as first byte
	 * of a command. Send a new byte, we should get back a NACK.
	 */
        p_err = port.write(cmd, 1);
        if (p_err != PORT_ERR_OK) {
            stderr.printf("Failed to send init to device\n");
            return STM32_ERR_UNKNOWN;
        }
        p_err = port.read(b, 1);
        if (p_err == PORT_ERR_OK && b[0] == STM32_NACK)
        return STM32_ERR_OK;
        stderr.printf("Failed to init device.\n");
        return STM32_ERR_UNKNOWN;
    }

/* find newer command by higher code */
    public static final byte newer(byte prev, byte a) {return (((prev) == STM32_CMD_ERR)
            ? (a)
            : (((prev) > (a)) ? (prev) : (a)));}

    static stm32_t stm32_init(port_interface port, boolean init)
    {
        byte val, buf[] = new byte[257];
        int len;
        stm32_t stm;
        int i, new_cmds;

        stm      = new stm32_t();
        stm.cmd = new stm32_cmd_t();
        stm.port = port;

        if ((port.flags() & PORT_CMD_INIT) != 0 && init)
            if (stm32_send_init_seq(stm) != STM32_ERR_OK)
                return null;

	/* get the version and read protection status  */
        if (stm32_send_command(stm, STM32_CMD_GVR) != STM32_ERR_OK) {
            stm32_close(stm);
            return null;
        }

	/* From AN, only UART bootloader returns 3 bytes */
        len = (port.flags() & PORT_GVR_ETX) != 0 ? 3 : 1;
        if (port.read(buf, len) != PORT_ERR_OK)
            return null;
        stm.version = buf[0];
        stm.option1 = (port.flags() & PORT_GVR_ETX) != 0 ? buf[1] : 0;
        stm.option2 = (port.flags() & PORT_GVR_ETX) != 0 ? buf[2] : 0;
        if (stm32_get_ack(stm) != STM32_ERR_OK) {
            stm32_close(stm);
            return null;
        }

	/* get the bootloader information */
        len = STM32_CMD_GET_LENGTH;
        if (port.cmd_get_reply() != null)
            for (i = 0; port.cmd_get_reply()[i].length != 0; i++)
                if (stm.version == port.cmd_get_reply()[i].version) {
                    len = port.cmd_get_reply()[i].length;
                    break;
                }
        if (stm32_guess_len_cmd(stm, STM32_CMD_GET, buf, len) != STM32_ERR_OK)
            return null;
        len = buf[0] + 1;
        stm.bl_version = buf[1];
        new_cmds = 0;
        for (i = 1; i < len; i++) {
            val = buf[i + 1];
            switch (val) {
                case STM32_CMD_GET:
                    stm.cmd.get = val; break;
                case STM32_CMD_GVR:
                    stm.cmd.gvr = val; break;
                case STM32_CMD_GID:
                    stm.cmd.gid = val; break;
                case STM32_CMD_RM:
                    stm.cmd.rm = val; break;
                case STM32_CMD_GO:
                    stm.cmd.go = val; break;
                case STM32_CMD_WM:
                case STM32_CMD_WM_NS:
                    stm.cmd.wm = newer(stm.cmd.wm, val);
                    break;
                case STM32_CMD_ER:
                case STM32_CMD_EE:
                case STM32_CMD_EE_NS:
                    stm.cmd.er = newer(stm.cmd.er, val);
                    break;
                case STM32_CMD_WP:
                case STM32_CMD_WP_NS:
                    stm.cmd.wp = newer(stm.cmd.wp, val);
                    break;
                case STM32_CMD_UW:
                case STM32_CMD_UW_NS:
                    stm.cmd.uw = newer(stm.cmd.uw, val);
                    break;
                case STM32_CMD_RP:
                case STM32_CMD_RP_NS:
                    stm.cmd.rp = newer(stm.cmd.rp, val);
                    break;
                case STM32_CMD_UR:
                case STM32_CMD_UR_NS:
                    stm.cmd.ur = newer(stm.cmd.ur, val);
                    break;
                case STM32_CMD_CRC:
                    stm.cmd.crc = newer(stm.cmd.crc, val);
                    break;
                default:
                    if (new_cmds++ == 0)
                        stderr.printf("GET returns unknown commands (0x%2x", val);
                    else
                        stderr.printf(", 0x%2x", val);
            }
        }
        if (new_cmds != 0)
            stderr.printf(")\n");
        if (stm32_get_ack(stm) != STM32_ERR_OK) {
            stm32_close(stm);
            return null;
        }

        if (stm.cmd.get == STM32_CMD_ERR
                || stm.cmd.gvr == STM32_CMD_ERR
                || stm.cmd.gid == STM32_CMD_ERR) {
            stderr.printf("Error: bootloader did not returned correct information from GET command\n");
            return null;
        }

	/* get the device ID */
        if (stm32_guess_len_cmd(stm, stm.cmd.gid, buf, 1) != STM32_ERR_OK) {
            stm32_close(stm);
            return null;
        }
        len = buf[0] + 1;
        if (len < 2) {
            stm32_close(stm);
            stderr.printf("Only %d bytes sent in the PID, unknown/unsupported device\n", len);
            return null;
        }
        stm.pid = (short)((buf[1] << 8) | buf[2]);
        if (len > 2) {
            stderr.printf("This bootloader returns %d extra bytes in PID:", len);
            for (i = 2; i <= len ; i++)
                stderr.printf(" %02x", buf[i]);
            stderr.printf("\n");
        }
        if (stm32_get_ack(stm) != STM32_ERR_OK) {
            stm32_close(stm);
            return null;
        }

//        stm.dev = devices;
//        while (stm.dev.id != 0x00 && stm.dev.id != stm.pid)
//            ++stm.dev;

        for (i = 0; i < devices.length; i++) {
            if (devices[i].id == stm.pid){
                stm.dev = devices[i];
                break;
            }
        }

        if (stm.dev == null) {
            stderr.printf("Unknown/unsupported device (Device ID: 0x%03x)\n", stm.pid);
            stm32_close(stm);
            return null;
        }

        return stm;
    }

    static void stm32_close(stm32_t stm)
    {

    }


    static byte[] address2buf(int address)
    {
        byte buf[] = new byte[5];

        buf[0] = (byte)(address >> 24); ///TODO >>> ?
        buf[1] = (byte)((address >> 16) & 0xFF);
        buf[2] = (byte)((address >> 8) & 0xFF);
        buf[3] = (byte)(address & 0xFF);
        buf[4] = (byte)(buf[0] ^ buf[1] ^ buf[2] ^ buf[3]);

        return buf;
    }

    static stm32_err_t stm32_read_memory(stm32_t stm, int address,
                                  byte data[], /*unsigned*/ int len)
    {
        port_interface port = stm.port;
        //byte buf[5];

        if (len == 0)
            return STM32_ERR_OK;

        if (len > 256) {
            stderr.printf("Error: READ length limit at 256 bytes\n");
            return STM32_ERR_UNKNOWN;
        }

        if (stm.cmd.rm == STM32_CMD_ERR) {
            stderr.printf("Error: READ command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

        if (stm32_send_command(stm, stm.cmd.rm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

//        buf[0] = (byte)(address >> 24);
//        buf[1] = (byte)((address >> 16) & 0xFF);
//        buf[2] = (byte)((address >> 8) & 0xFF);
//        buf[3] = (byte)(address & 0xFF);
//        buf[4] = (byte)(buf[0] ^ buf[1] ^ buf[2] ^ buf[3]);

        byte buf[] = address2buf(address);

        if (port.write(buf, 5) != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;
        if (stm32_get_ack(stm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        if (stm32_send_command(stm, (byte)(len - 1)) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        if (port.read(data, len) != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;

        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_write_memory(stm32_t stm, int address,
                                   byte data[], /*unsigned*/ int len, int start)
    {
        port_interface port = stm.port;
        byte cs, buf[] = new byte[256 + 2];
        /*unsigned*/ int i, aligned_len;
        stm32_err_t s_err;

        if (len == 0)
            return STM32_ERR_OK;

        if (len > 256) {
            stderr.printf("Error: READ length limit at 256 bytes\n");
            return STM32_ERR_UNKNOWN;
        }

	/* must be 32bit aligned */
        if ((address & 0x3) != 0) {
            stderr.printf("Error: WRITE address must be 4 byte aligned\n");
            return STM32_ERR_UNKNOWN;
        }

        if (stm.cmd.wm == STM32_CMD_ERR) {
            stderr.printf("Error: WRITE command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

	/* send the address and checksum */
        if (stm32_send_command(stm, stm.cmd.wm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

//        buf[0] = address >> 24;
//        buf[1] = (address >> 16) & 0xFF;
//        buf[2] = (address >> 8) & 0xFF;
//        buf[3] = address & 0xFF;
//        buf[4] = buf[0] ^ buf[1] ^ buf[2] ^ buf[3];

        buf = address2buf(address);

        if (port.write(buf, 5) != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;
        if (stm32_get_ack(stm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        buf = new byte[256 + 2];

        aligned_len = (len + 3) & ~3;
        cs = (byte)(aligned_len - 1);
        buf[0] = cs;//aligned_len - 1;
        for (i = 0; i < len; i++) {
            cs ^= data[i];
            buf[i + 1] = data[i];
        }
	/* padding data */
        for (i = len; i < aligned_len; i++) {
            cs ^= (byte)0xFF;
            buf[i + 1] = (byte)0xFF;
        }
        buf[aligned_len + 1] = cs;
        if (port.write(buf, aligned_len + 2, start) != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;

        s_err = stm32_get_ack_timeout(stm, STM32_BLKWRITE_TIMEOUT);
        if (s_err != STM32_ERR_OK) {
            if ((port.flags() & PORT_STRETCH_W) != 0
                    && stm.cmd.wm != STM32_CMD_WM_NS)
                stm32_warn_stretching("write");
            return STM32_ERR_UNKNOWN;
        }
        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_wunprot_memory(stm32_t stm)
    {
        port_interface port = stm.port;
        stm32_err_t s_err;

        if (stm.cmd.uw == STM32_CMD_ERR) {
            stderr.printf("Error: WRITE UNPROTECT command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

        if (stm32_send_command(stm, stm.cmd.uw) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        s_err = stm32_get_ack_timeout(stm, STM32_WUNPROT_TIMEOUT);
        if (s_err == STM32_ERR_NACK) {
            stderr.printf("Error: Failed to WRITE UNPROTECT\n");
            return STM32_ERR_UNKNOWN;
        }
        if (s_err != STM32_ERR_OK) {
            if ((port.flags() & PORT_STRETCH_W) != 0
                    && stm.cmd.uw != STM32_CMD_UW_NS)
                stm32_warn_stretching("WRITE UNPROTECT");
            return STM32_ERR_UNKNOWN;
        }
        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_wprot_memory(stm32_t stm)
    {
        port_interface port = stm.port;
        stm32_err_t s_err;

        if (stm.cmd.wp == STM32_CMD_ERR) {
            stderr.printf("Error: WRITE PROTECT command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

        if (stm32_send_command(stm, stm.cmd.wp) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        s_err = stm32_get_ack_timeout(stm, STM32_WPROT_TIMEOUT);
        if (s_err == STM32_ERR_NACK) {
            stderr.printf("Error: Failed to WRITE PROTECT\n");
            return STM32_ERR_UNKNOWN;
        }
        if (s_err != STM32_ERR_OK) {
            if ((port.flags() & PORT_STRETCH_W) != 0
                    && stm.cmd.wp != STM32_CMD_WP_NS)
                stm32_warn_stretching("WRITE PROTECT");
            return STM32_ERR_UNKNOWN;
        }
        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_runprot_memory(stm32_t stm)
    {
        port_interface port = stm.port;
        stm32_err_t s_err;

        if (stm.cmd.ur == STM32_CMD_ERR) {
            stderr.printf("Error: READOUT UNPROTECT command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

        if (stm32_send_command(stm, stm.cmd.ur) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        s_err = stm32_get_ack_timeout(stm, STM32_MASSERASE_TIMEOUT);
        if (s_err == STM32_ERR_NACK) {
            stderr.printf("Error: Failed to READOUT UNPROTECT\n");
            return STM32_ERR_UNKNOWN;
        }
        if (s_err != STM32_ERR_OK) {
            if ((port.flags() & PORT_STRETCH_W) != 0
                    && stm.cmd.ur != STM32_CMD_UR_NS)
                stm32_warn_stretching("READOUT UNPROTECT");
            return STM32_ERR_UNKNOWN;
        }
        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_readprot_memory(stm32_t stm)
    {
        port_interface port = stm.port;
        stm32_err_t s_err;

        if (stm.cmd.rp == STM32_CMD_ERR) {
            stderr.printf("Error: READOUT PROTECT command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

        if (stm32_send_command(stm, stm.cmd.rp) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        s_err = stm32_get_ack_timeout(stm, STM32_RPROT_TIMEOUT);
        if (s_err == STM32_ERR_NACK) {
            stderr.printf("Error: Failed to READOUT PROTECT\n");
            return STM32_ERR_UNKNOWN;
        }
        if (s_err != STM32_ERR_OK) {
            if ((port.flags() & PORT_STRETCH_W) != 0
                    && stm.cmd.rp != STM32_CMD_RP_NS)
                stm32_warn_stretching("READOUT PROTECT");
            return STM32_ERR_UNKNOWN;
        }
        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_mass_erase(stm32_t stm)
    {
        port_interface port = stm.port;
        stm32_err_t s_err;
        byte buf[] = new byte[3];

        if (stm32_send_command(stm, stm.cmd.er) != STM32_ERR_OK) {
            stderr.printf("Can't initiate chip mass erase!\n");
            return STM32_ERR_UNKNOWN;
        }

	/* regular erase (0x43) */
        if (stm.cmd.er == STM32_CMD_ER) {
            s_err = stm32_send_command_timeout(stm, (byte)0xFF, STM32_MASSERASE_TIMEOUT);
            if (s_err != STM32_ERR_OK) {
                if ((port.flags() & PORT_STRETCH_W) != 0)
                    stm32_warn_stretching("mass erase");
                return STM32_ERR_UNKNOWN;
            }
            return STM32_ERR_OK;
        }

	/* extended erase */
        buf[0] = (byte)0xFF;	/* 0xFFFF the magic number for mass erase */
        buf[1] = (byte)0xFF;
        buf[2] = 0x00;  /* checksum */
        if (port.write(buf, 3) != PORT_ERR_OK) {
            stderr.printf("Mass erase error.\n");
            return STM32_ERR_UNKNOWN;
        }
        s_err = stm32_get_ack_timeout(stm, STM32_MASSERASE_TIMEOUT);
        if (s_err != STM32_ERR_OK) {
            stderr.printf("Mass erase failed. Try specifying the number of pages to be erased.\n");
            if ((port.flags() & PORT_STRETCH_W) != 0
                    && stm.cmd.er != STM32_CMD_EE_NS)
                stm32_warn_stretching("mass erase");
            return STM32_ERR_UNKNOWN;
        }
        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_pages_erase(stm32_t stm, int spage, int pages)
    {
        port_interface port = stm.port;
        stm32_err_t s_err;
        port_err_t p_err;
        int pg_num;
        byte pg_byte;
        byte cs = 0;
        byte buf[];
        int i = 0;

	/* The erase command reported by the bootloader is either 0x43, 0x44 or 0x45 */
	/* 0x44 is Extended Erase, a 2 byte based protocol and needs to be handled differently. */
	/* 0x45 is clock no-stretching version of Extended Erase for I2C port. */
        if (stm32_send_command(stm, stm.cmd.er) != STM32_ERR_OK) {
            stderr.printf("Can't initiate chip mass erase!\n");
            return STM32_ERR_UNKNOWN;
        }

	/* regular erase (0x43) */
        if (stm.cmd.er == STM32_CMD_ER) {
            buf = new byte[1 + pages + 1];
//            buf = malloc(1 + pages + 1);
//            if (!buf)
//                return STM32_ERR_UNKNOWN;

            buf[i++] = (byte)(pages - 1);
            cs ^= (pages-1);
            for (pg_num = spage; pg_num < (pages + spage); pg_num++) {
                buf[i++] = (byte)pg_num;
                cs ^= pg_num;
            }
            buf[i++] = cs;
            p_err = port.write(buf, i);
//            free(buf);
            if (p_err != PORT_ERR_OK) {
                stderr.printf("Erase failed.\n");
                return STM32_ERR_UNKNOWN;
            }
            s_err = stm32_get_ack_timeout(stm, pages * STM32_PAGEERASE_TIMEOUT);
            if (s_err != STM32_ERR_OK) {
                if ((port.flags() & PORT_STRETCH_W) != 0)
                    stm32_warn_stretching("erase");
                return STM32_ERR_UNKNOWN;
            }
            return STM32_ERR_OK;
        }

	/* extended erase */
        buf = new byte[2 + 2 * pages + 1];
//        buf = malloc(2 + 2 * pages + 1);
//        if (!buf)
//            return STM32_ERR_UNKNOWN;

	/* Number of pages to be erased - 1, two bytes, MSB first */
        pg_byte = (byte)((pages - 1) >> 8);
        buf[i++] = pg_byte;
        cs ^= pg_byte;
        pg_byte = (byte)((pages - 1) & 0xFF);
        buf[i++] = pg_byte;
        cs ^= pg_byte;

        for (pg_num = spage; pg_num < spage + pages; pg_num++) {
            pg_byte = (byte)(pg_num >> 8);
            cs ^= pg_byte;
            buf[i++] = pg_byte;
            pg_byte = (byte)(pg_num & 0xFF);
            cs ^= pg_byte;
            buf[i++] = pg_byte;
        }
        buf[i++] = cs;
        p_err = port.write(buf, i);
//        free(buf);
        if (p_err != PORT_ERR_OK) {
            stderr.printf("Page-by-page erase error.\n");
            return STM32_ERR_UNKNOWN;
        }

        s_err = stm32_get_ack_timeout(stm, pages * STM32_PAGEERASE_TIMEOUT);
        if (s_err != STM32_ERR_OK) {
            stderr.printf("Page-by-page erase failed. Check the maximum pages your device supports.\n");
            if ((port.flags() & PORT_STRETCH_W) != 0
                    && stm.cmd.er != STM32_CMD_EE_NS)
                stm32_warn_stretching("erase");
            return STM32_ERR_UNKNOWN;
        }

        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_erase_memory(stm32_t stm, int spage, int pages)
    {
        int n;
        stm32_err_t s_err;

        if (pages == 0 || spage > STM32_MAX_PAGES ||
                ((pages != STM32_MASS_ERASE) && ((spage + pages) > STM32_MAX_PAGES)))
            return STM32_ERR_OK;

        if (stm.cmd.er == STM32_CMD_ERR) {
            stderr.printf("Error: ERASE command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

        if (pages == STM32_MASS_ERASE) {
		/*
		 * Not all chips support mass erase.
		 * Mass erase can be obtained executing a "readout protect"
		 * followed by "readout un-protect". This method is not
		 * suggested because can hang the target if a debug SWD/JTAG
		 * is connected. When the target enters in "readout
		 * protection" mode it will consider the debug connection as
		 * a tentative of intrusion and will hang.
		 * Erasing the flash page-by-page is the safer way to go.
		 */
            if ((stm.dev.flags & F_NO_ME) == 0)
                return stm32_mass_erase(stm);

            pages = flash_addr_to_page_ceil(stm.dev.fl_end);
        }

	/*
	 * Some device, like STM32L152, cannot erase more than 512 pages in
	 * one command. Split the call.
	 */
        while (pages != 0) {
            n = (pages <= 512) ? pages : 512;
            s_err = stm32_pages_erase(stm, spage, n);
            if (s_err != STM32_ERR_OK)
                return s_err;
            spage += n;
            pages -= n;
        }
        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_run_raw_code(stm32_t stm,
                                          int target_address,
                                          byte code[], int code_size)
    {
        byte stack_le[] = le_u32(0x20002000);
        byte code_address_le[] = le_u32(target_address + 8 + 1); // thumb mode address (!)
        int length = code_size + 8;
        byte mem[];
        int pos;
        int address, w;

	/* Must be 32-bit aligned */
        if ((target_address & 0x3) != 0) {
            stderr.printf("Error: code address must be 4 byte aligned\n");
            return STM32_ERR_UNKNOWN;
        }

        mem = new byte[length];
//        mem = malloc(length);
//        if (!mem)
//            return STM32_ERR_UNKNOWN;

        System.arraycopy(stack_le, 0, mem, 0, stack_le.length);
        System.arraycopy(code_address_le, 0, mem, 4, code_address_le.length);
        System.arraycopy(code, 0, mem, 8, code_size);
//        memcpy(mem, &stack_le, sizeof(int));
//        memcpy(mem + 4, &code_address_le, sizeof(int));
//        memcpy(mem + 8, code, code_size);

        pos = 0;//mem;
        address = target_address;
        while (length > 0) {
            w = length > 256 ? 256 : length;
            if (stm32_write_memory(stm, address, mem, w, pos) != STM32_ERR_OK) {
//                free(mem);
                return STM32_ERR_UNKNOWN;
            }

            address += w;
            pos += w;
            length -= w;
        }

//        free(mem);
        return stm32_go(stm, target_address);
    }

    static stm32_err_t stm32_go(stm32_t stm, int address)
    {
        port_interface port = stm.port;
        //byte buf[5];

        if (stm.cmd.go == STM32_CMD_ERR) {
            stderr.printf("Error: GO command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

        if (stm32_send_command(stm, stm.cmd.go) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

//        buf[0] = address >> 24;
//        buf[1] = (address >> 16) & 0xFF;
//        buf[2] = (address >> 8) & 0xFF;
//        buf[3] = address & 0xFF;
//        buf[4] = buf[0] ^ buf[1] ^ buf[2] ^ buf[3];

        byte buf[] = address2buf(address);

        if (port.write(buf, 5) != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;

        if (stm32_get_ack(stm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;
        return STM32_ERR_OK;
    }

    static stm32_err_t stm32_reset_device(stm32_t stm)
    {
        int target_address = stm.dev.ram_start;

        if ((stm.dev.flags & F_OBLL) != 0) {
		/* set the OBL_LAUNCH bit to reset device (see RM0360, 2.5) */
            return stm32_run_raw_code(stm, target_address, stm_obl_launch_code, stm_obl_launch_code_length);
        } else {
            return stm32_run_raw_code(stm, target_address, stm_reset_code, stm_reset_code_length);
        }
    }

    static stm32_err_t stm32_crc_memory(stm32_t stm, int address,
                                 int length, IntPtr crc)
    {
        port_interface port = stm.port;
        //byte buf[5];

        if ((address & 0x3) != 0 || (length & 0x3) != 0) {
            stderr.printf("Start and end addresses must be 4 byte aligned\n");
            return STM32_ERR_UNKNOWN;
        }

        if (stm.cmd.crc == STM32_CMD_ERR) {
            stderr.printf("Error: CRC command not implemented in bootloader.\n");
            return STM32_ERR_NO_CMD;
        }

        if (stm32_send_command(stm, stm.cmd.crc) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

//        buf[0] = address >> 24;
//        buf[1] = (address >> 16) & 0xFF;
//        buf[2] = (address >> 8) & 0xFF;
//        buf[3] = address & 0xFF;
//        buf[4] = buf[0] ^ buf[1] ^ buf[2] ^ buf[3];

        byte buf[] = address2buf(address);

        if (port.write(buf, 5) != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;

        if (stm32_get_ack(stm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

//        buf[0] = length >> 24;
//        buf[1] = (length >> 16) & 0xFF;
//        buf[2] = (length >> 8) & 0xFF;
//        buf[3] = length & 0xFF;
//        buf[4] = buf[0] ^ buf[1] ^ buf[2] ^ buf[3];

        buf = address2buf(address);

        if (port.write(buf, 5) != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;

        if (stm32_get_ack(stm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        if (stm32_get_ack(stm) != STM32_ERR_OK)
            return STM32_ERR_UNKNOWN;

        if (port.read(buf, 5) != PORT_ERR_OK)
            return STM32_ERR_UNKNOWN;

        if (buf[4] != (buf[0] ^ buf[1] ^ buf[2] ^ buf[3]))
            return STM32_ERR_UNKNOWN;

        crc.value = (buf[0] << 24) | (buf[1] << 16) | (buf[2] << 8) | buf[3];
        return STM32_ERR_OK;
    }

/*
 * CRC computed by STM32 is similar to the standard crc32_be()
 * implemented, for example, in Linux kernel in ./lib/crc32.c
 * But STM32 computes it on units of 32 bits word and swaps the
 * bytes of the word before the computation.
 * Due to byte swap, I cannot use any CRC available in existing
 * libraries, so here is a simple not optimized implementation.
 */
    public static final int CRCPOLY_BE	=0x04c11db7;
            public static final int CRC_MSBMASK	=0x80000000;
            public static final int CRC_INIT_VALUE	=0xFFFFFFFF;

    static int stm32_sw_crc(int crc, byte buf[], /*unsigned*/ int len)
    {
        int i;
        int data;

        if ((len & 0x3) != 0) {
            stderr.printf("Buffer length must be multiple of 4 bytes\n");
            return 0;
        }

        int j = 0;
        while (len != 0) {
            data = buf[j++];
            data |= buf[j++] << 8;
            data |= buf[j++] << 16;
            data |= buf[j++] << 24;
            len -= 4;

            crc ^= data;

            for (i = 0; i < 32; i++)
                if ((crc & CRC_MSBMASK) != 0)
                    crc = (crc << 1) ^ CRCPOLY_BE;
                else
                    crc = (crc << 1);
        }
        return crc;
    }

    static stm32_err_t stm32_crc_wrapper(stm32_t stm, int address,
                                  int length, IntPtr crc)
    {
        byte buf[] = new byte[256];
        int start, total_len, len, current_crc;

        if ((address & 0x3) != 0 || (length & 0x3) != 0) {
            stderr.printf("Start and end addresses must be 4 byte aligned\n");
            return STM32_ERR_UNKNOWN;
        }

        if (stm.cmd.crc != STM32_CMD_ERR)
            return stm32_crc_memory(stm, address, length, crc);

        start = address;
        total_len = length;
        current_crc = CRC_INIT_VALUE;
        while (length != 0) {
            len = length > 256 ? 256 : length;
            if (stm32_read_memory(stm, address, buf, len) != STM32_ERR_OK) {
                stderr.printf(
                        "Failed to read memory at address 0x%08x, target write-protected?\n",
                        address);
                return STM32_ERR_UNKNOWN;
            }
            current_crc = stm32_sw_crc(current_crc, buf, len);
            length -= len;
            address += len;

            stderr.printf(
                    "\rCRC address 0x%08x (%.2f%%) ",
                    address,
                    (100.0f / (float)total_len) * (float)(address - start)
            );
            stderr.flush();
        }
        stderr.printf("Done.\n");
        crc.value = current_crc;
        return STM32_ERR_OK;
    }


}
