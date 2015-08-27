// Original version copyright (C) 2006, Erik Giberti (AF-Design), and licensed GNU GPL

Ext.namespace('Ext.ux');

Ext.ux.UUID = function(config) {
    if(!config) {
    	// on creation of a UUID object, set it's initial value
    	this.id = this.createUUID();

    } else {
        // if a config is passed, use a connection object to request the UUID from the server.
        Ext.Ajax.request(config);
    }
}

Ext.ux.UUID.prototype.valueOf = function(){ return this.id; };
Ext.ux.UUID.prototype.toString = function(){ return this.id; };

//
// INSTANCE SPECIFIC METHODS
//

Ext.ux.UUID.prototype.createUUID = function() {
        // Loose interpretation of the specification DCE 1.1: Remote Procedure Call
        // described at http://www.opengroup.org/onlinepubs/009629399/apdxa.htm#tagtcjh_37
        // since JavaScript doesn't allow access to internal systems, the last 48 bits 
        // of the node section is made up using a series of random numbers (6 octets long).

        var dg = new Date(1582, 10, 15, 0, 0, 0, 0).getTime();
        var dc = new Date().getTime();
        var t = (dg < 0) ? Math.abs(dg) + dc : dc - dg;
        var h = '-';
        var tl = Ext.ux.UUID.getIntegerBits(t, 0, 31);
        var tm = Ext.ux.UUID.getIntegerBits(t, 32, 47);
        var thv = Ext.ux.UUID.getIntegerBits(t, 48, 59) + '1'; // version 1, security version is 2
        var csar = Ext.ux.UUID.getIntegerBits(Math.randRange(0, 4095), 0, 7);
        var csl = Ext.ux.UUID.getIntegerBits(Math.randRange(0, 4095), 0, 7);
  
        // since detection of anything about the machine/browser is far too buggy, 
        // include some more random numbers here
        // if nic or at least an IP can be obtained reliably, that should be put in
        // here instead.
        var n = Ext.ux.UUID.getIntegerBits(Math.randRange(0, 8191), 0, 7) + 
                        Ext.ux.UUID.getIntegerBits(Math.randRange(0, 8191), 8, 15) + 
                        Ext.ux.UUID.getIntegerBits(Math.randRange(0, 8191), 0, 7) + 
                        Ext.ux.UUID.getIntegerBits(Math.randRange(0, 8191), 8, 15) + 
                        Ext.ux.UUID.getIntegerBits(Math.randRange(0, 8191), 0, 15); // this last number is two octets long
        return tl + h + tm + h + thv + h + csar + csl + n; 
}

//
// GENERAL METHODS (Not instance specific)
//

// Pull out only certain bits from a very large integer, used to get the time
// code information for the first part of a UUID. Will return zero's if there 
// aren't enough bits to shift where it needs to.
Ext.ux.UUID.getIntegerBits = function(val, start, end) {
    var base16 = Ext.ux.UUID.returnBase(val, 16);
    var quadArray = base16.split('');
    var quadString = '';
    var i = 0;
    for(i = Math.floor(start / 4); i <= Math.floor(end / 4); i++) {
        if(!quadArray[i] || quadArray[i] == '') quadString += '0';
        else quadString += quadArray[i];
    }
    return quadString;
}

// In base 16: 0=0, 5=5, 10=A, 15=F
Ext.ux.UUID.returnBase = function(number, base) {
    return number.toString(base).toUpperCase();
}

/**
 * @class Math
 */
Ext.applyIf(Math, {
    /**
     * extend Math class with a randRange method
     * @return {Number} A random number greater than or equal to min and less than or equal to max.
     */
    randRange : function(min, max) {
        return Math.max(Math.min(Math.round(Math.random() * max), max), min);
    }
});