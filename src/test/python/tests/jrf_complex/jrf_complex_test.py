import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def join_reg_fork_data(dut):
    """It should join and then fork asymmetric data on two input and output channels"""
    # Reset.
    dut.reset.value = 1
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    dut.io_in1_data.value = 0xab
    dut.io_in2_data.value = 0xcd
    dut.io_out1_ack.value = 0
    dut.io_out2_ack.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")
    assert dut.io_out1_data.value == 0
    assert dut.io_out2_data.value == 4
    assert dut.io_out1_req.value == 0
    assert dut.io_out2_req.value == 1

    # Bring in some new data
    dut.io_in1_req.value = 1
    await Timer(1, "ns")
    dut.io_in2_req.value = 1
    await Timer(1, "ns")
    # Data cannot propagate until consumer has received data on out2
    dut.io_out2_ack.value = 1
    await Edge(dut.io_out1_req)
    await Timer(1)  # Must wait a small timestep after req triggers for register to trigger
    assert dut.io_out1_req.value == 1
    assert dut.io_out2_req.value == 0
    assert dut.io_out1_data.value == 0xa
    assert dut.io_out2_data.value == (0xb << 10) | 0xcd
    await Timer(1, "ns")
    dut.io_out1_ack.value = 1
    dut.io_out2_ack.value = 0
    await Timer(1, "ns")

    # Bring in some more data
    dut.io_in1_data.value = 0xee
    dut.io_in2_data.value = 0x333
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    await Edge(dut.io_out2_req)
    await Timer(1)  # Must wait a small timestep after req triggers for other registers to trigger
    assert dut.io_out1_req.value == 0
    assert dut.io_out2_req.value == 1
    assert dut.io_out1_data.value == 0xe
    assert dut.io_out2_data.value == (0xe << 10) | 0x333
    await Timer(2, "ns")