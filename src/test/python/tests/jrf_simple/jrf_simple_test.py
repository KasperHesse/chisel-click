import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def join_reg_fork_data(dut):
    """It should join and then fork symmetric data on two input and output channels"""
    # Reset
    dut.reset.value = 1
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    dut.io_in1_data.value = 42
    dut.io_in2_data.value = 84
    dut.io_out1_ack.value = 0
    dut.io_out2_ack.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")
    assert dut.io_out1_data.value == 0
    assert dut.io_out2_data.value == 0

    # Bring in some new data
    dut.io_in1_req.value = 1
    await Timer(1, "ns")
    dut.io_in2_req.value = 1
    await Edge(dut.io_out1_req)
    await Timer(1)  # Must wait a small timestep after req triggers for register to trigger
    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 1
    assert dut.io_out1_data.value == (42 << 8) | 84
    assert dut.io_out2_data.value == (42 << 8) | 84
    await Timer(1, "ns")
    dut.io_out1_ack.value = 1
    dut.io_out2_ack.value = 1
    await Timer(1, "ns")

    # Bring in some more data
    dut.io_in1_data.value = 0xab
    dut.io_in2_data.value = 0xcd
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    await Edge(dut.io_out2_req)
    await Timer(1)  # Must wait a small timestep after req triggers for other registers to trigger
    assert dut.io_out2_req.value == 0
    assert dut.io_out1_req.value == 0
    assert dut.io_out1_data.value == 0xabcd
    assert dut.io_out2_data.value == 0xabcd
    await Timer(2, "ns")

@cocotb.test()
async def no_new_handshake(dut):
    """It should not accept new handshakes before the first handshake has finished"""
    # Reset
    dut.reset.value = 1
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    dut.io_in1_data.value = 42
    dut.io_in2_data.value = 84
    dut.io_out1_ack.value = 0
    dut.io_out2_ack.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")
    assert dut.io_out1_data.value == 0
    assert dut.io_out2_data.value == 0

    # Bring in some new data
    dut.io_in1_req.value = 1
    await Timer(1, "ns")
    dut.io_in2_req.value = 1
    await Edge(dut.io_out1_req)
    await Timer(1)  # Must wait a small timestep after req triggers for register to trigger
    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 1
    assert dut.io_out1_data.value == (42 << 8) | 84
    assert dut.io_out2_data.value == (42 << 8) | 84
    await Timer(1, "ns")

    # Attempt to change input data without both output channels acknowledging the input
    dut.io_out1_ack.value = 1
    dut.io_out2_ack.value = 0

    dut.io_in1_data.value = 0xab
    dut.io_in2_data.value = 0xcd
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    await Timer(10, "ns")
    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 1
    assert dut.io_out1_data.value == (42 << 8) | 84
    assert dut.io_out2_data.value == (42 << 8) | 84

    # Flip the last acknowledge, let data propagate
    dut.io_out2_ack.value = 1
    await Edge(dut.io_out1_req)
    await Timer(1)
    assert dut.io_out2_req.value == 0
    assert dut.io_out1_req.value == 0
    assert dut.io_out1_data.value == 0xabcd
    assert dut.io_out2_data.value == 0xabcd
