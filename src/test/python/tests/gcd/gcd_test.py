import cocotb
from cocotb.triggers import Timer, Edge


def gcd(a: int, b: int) -> int:
    while a != b:
        if a > b:
            a = a - b
        else:
            b = b - a
    return a


async def toggle(signal):
    """Flips the value of binary signal wire"""
    if signal.value == 0:
        signal.value = 1
    else:
        signal.value = 0


async def reset(dut):
    # Reset sequence
    dut.reset.value = 1
    dut.io_in_data_a.value = 0
    dut.io_in_data_b.value = 0
    dut.io_out_ack.value = 0
    dut.io_in_req.value = 0
    await Timer(5, "ns")
    dut.reset.value = 0
    await Timer(1)


async def compute_gcd(dut, a, b):
    """Performs all of the data poking necessary to compute the GCD of two values"""
    # Toggle req to start the circuit
    dut.io_in_data_a.value = a
    dut.io_in_data_b.value = b
    await toggle(dut.io_in_req)
    await Edge(dut.io_out_req)
    await Timer(1)
    assert dut.io_out_data_a == gcd(a, b)
    assert dut.io_out_data_b == gcd(a, b)
    await Timer(1, "ns")
    await toggle(dut.io_out_ack)
    await Timer(2, "ns")


@cocotb.test()
async def check_in_req(dut):
    await reset(dut)
    # Custom compute gcd without acknowledge in the end
    dut.io_in_data_a.value = 4
    dut.io_in_data_b.value = 2
    await toggle(dut.io_in_req)
    await Edge(dut.io_out_req)
    await Timer(1)
    assert dut.io_out_data_a == gcd(4, 2)
    assert dut.io_out_data_b == gcd(4, 2)

    # End of gcd
    await Timer(30, "ns")
    await toggle(dut.io_in_req)
    await Timer(30, "ns")
    await toggle(dut.io_in_req)
    await Timer(50, "ns")
    await toggle(dut.io_in_req)
    await Timer(30, "ns")
    await toggle(dut.io_in_req)
    await Timer(30, "ns")
    await toggle(dut.io_in_req)

@cocotb.test()
async def gcd_4_2(dut):
    """It should compute the GCD of 4 and 2"""
    await reset(dut)
    await compute_gcd(dut, 4, 2)


@cocotb.test()
async def gcd_5_15(dut):
    """It should compute the GCD of 5 and 15"""
    await reset(dut)
    await compute_gcd(dut, 5, 15)


@cocotb.test()
async def multiple_gcd(dut):
    """It should compute multiple GCD's in a row"""
    await reset(dut)
    await compute_gcd(dut, 9, 12)
    await compute_gcd(dut, 42, 16)
    await compute_gcd(dut, 42, 18)


@cocotb.test()
async def await_completion(dut):
    """It should not take in new inputs before outputs have been generated"""
    await reset(dut)
    dut.io_in_data_a.value = 18
    dut.io_in_data_b.value = 15
    await toggle(dut.io_in_req)
    await Edge(dut.io_in_ack)
    await Timer(2, "ns")

    # Once acknowledged, we change the input data already
    # Should not impact the output
    dut.io_in_data_a.value = 6
    dut.io_in_data_b.value = 4
    await toggle(dut.io_in_req)

    # Await the output
    await Edge(dut.io_out_req)
    await Timer(1)
    assert dut.io_out_data_a == gcd(18, 15)
    assert dut.io_out_data_b == gcd(18, 15)
    # Input-ack should still be high, as it hasn't taken in new data yet
    assert dut.io_in_ack.value == 1
    await Timer(1, "ns")
    # Acknowledge first data received
    await toggle(dut.io_out_ack)
    # Now, wait for next data to be received
    await Edge(dut.io_out_req)
    await Timer(1)
    assert dut.io_out_data_a == gcd(6, 4)
    assert dut.io_out_data_b == gcd(6, 4)
    await Timer(2, "ns")

