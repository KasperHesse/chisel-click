import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import RisingEdge, FallingEdge


@cocotb.test()
async def add_numbers(dut):
    """It should add two numbers combinationally"""
    # reset
    dut.reset.value = 1
    dut.io_in_req.value = 0
    dut.io_in_data_a.value = 42
    dut.io_in_data_b.value = 84
    dut.io_out_ack.value = 0
    await Timer(1)
    dut.reset.value = 0
    await Timer(1)

    assert dut.io_out_req.value == 0
    assert dut.io_in_ack.value == 0
    dut.io_in_req.value = 1
    await Timer(1)

    assert dut.io_out_req.value == 1
    assert dut.io_out_data.value == (42+84)
    dut.io_out_ack.value = 1
    await Timer(1)

    assert dut.io_in_ack.value == 1
    dut.io_in_req.value = 0
    await Timer(1)

    assert dut.io_out_req.value == 0
    dut.io_out_ack.value = 0
    await Timer(1)

    assert dut.io_in_ack.value == 0
