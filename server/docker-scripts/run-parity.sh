if [ ! -f ~/.local/share/io.parity.ethereum/password ]; then
  echo "First time running parity"
  #Parity doesnt seem to allow unlocking of accounts on first run!
  #Need to run then kill, then run again unlocked.
  nohup parity --chain dev &
  export PID=$!
  sleep 5
  kill -9 $PID

  echo "" > ~/.local/share/io.parity.ethereum/password
fi

# Create log file if it doesn't exist
if [ ! -f /data/parity-logs/parity.log ]; then
	mkdir -p /data/parity-logs
	touch /data/parity-logs/parity.log
fi

nohup parity \
	--chain dev \
	--reseal-min-period 0 \
	--jsonrpc-cors '*' \
	--jsonrpc-hosts '0.0.0.0' \
	--jsonrpc-apis 'all' \
	--geth \
	--force-ui \
	--unsafe-expose \
	--unlock "0x00a329c0648769A73afAc7F9381E08FB43dBEA72" \
	--log-file /data/parity-logs/parity.log \
	--password "$HOME/.local/share/io.parity.ethereum/password" > /data/parity-logs/nohup.out 2>&1&

sleep 5

echo "Parity Running..."
#Keeps container alive
tail -f /dev/null
