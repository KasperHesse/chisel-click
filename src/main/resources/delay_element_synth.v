module delay_element_synth #(parameter DELAY = 10)
  (reqIn,
reqOut);
  input reqIn;
  output reqOut;

    wire [DELAY:0] s_connect;
    int y_placement[] = '{0,1,0,1,0,1,0,1,2,3,2,3,2,3,2,3,4,5,4,5,4,5,4,5,6,7,6,7,6,7};
    genvar i;

    generate
        for(i=0; i<DELAY; i++) begin
// (* rloc = '{"X0Y", "0" + y_placement[i]} *)
          lut1#(.init(2'b10)) delay_lut (
            .I0(s_connect[i]),
            .O(s_connect[i+1])
          );
        end
    endgenerate

                    assign reqOut = s_connect[DELAY];
endmodule